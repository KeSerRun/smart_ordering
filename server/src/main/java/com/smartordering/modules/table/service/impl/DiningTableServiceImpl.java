package com.smartordering.modules.table.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.smartordering.common.enums.WsEventType;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.framework.config.MinioConfig;
import com.smartordering.framework.websocket.WsService;
import com.smartordering.modules.table.dto.TableCreateDTO;
import com.smartordering.modules.table.dto.TableUpdateDTO;
import com.smartordering.modules.table.entity.DiningTable;
import com.smartordering.modules.table.mapper.DiningTableMapper;
import com.smartordering.modules.table.service.DiningTableService;
import com.smartordering.modules.table.vo.DiningTableVO;
import com.smartordering.modules.table.vo.QrCodeTaskVO;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Dining table service implementation.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl implements DiningTableService {

    private final DiningTableMapper diningTableMapper;
        private final MinioClient minioClient;
        private final MinioConfig minioConfig;
        private final WsService wsService;

    /** In-memory QR task metadata (taskId -> task). */
    private static final Map<String, QrCodeTaskVO> QR_TASKS = new ConcurrentHashMap<>();
    /** In-memory packed zip bytes for download-all tasks (taskId -> bytes). */
    private static final Map<String, byte[]> QR_TASK_FILES = new ConcurrentHashMap<>();

    private static final String QR_PREFIX = "table/qrcode/";

    @Override
    public DiningTableVO getByCode(String code) {
        DiningTable table = diningTableMapper.selectOne(
                new LambdaQueryWrapper<DiningTable>().eq(DiningTable::getCode, code));
        if (table == null) {
            throw new BusinessException("Table not found");
        }
        return toVO(table);
    }

    @Override
    public List<DiningTableVO> list(Long areaId) {
        return listAll().stream()
                .filter(t -> areaId == null || areaId.equals(t.getAreaId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DiningTableVO> listAll() {
        LambdaQueryWrapper<DiningTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DiningTable::getAreaId).orderByAsc(DiningTable::getCode);
        return diningTableMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Long createTable(TableCreateDTO dto) {
        if (!StringUtils.hasText(dto.getCode())) {
            throw new BusinessException("Table code is required");
        }
        if (diningTableMapper.selectCount(new LambdaQueryWrapper<DiningTable>()
                .eq(DiningTable::getCode, dto.getCode())) > 0) {
            throw new BusinessException("Table code already exists");
        }
        DiningTable table = new DiningTable();
        BeanUtils.copyProperties(dto, table);
        table.setId(null);
        if (table.getStatus() == null) {
            table.setStatus(0);
        }
        table.setAreaName(dto.getAreaName());
        diningTableMapper.insert(table);
        return table.getId();
    }

    @Override
    public void updateTable(TableUpdateDTO dto) {
        if (dto.getId() == null || diningTableMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Table not found");
        }
        DiningTable table = new DiningTable();
        BeanUtils.copyProperties(dto, table);
        diningTableMapper.updateById(table);
    }

    @Override
    public void deleteTable(Long id) {
        if (diningTableMapper.selectById(id) == null) {
            throw new BusinessException("Table not found");
        }
        diningTableMapper.deleteById(id);
    }

    @Override
    public void markClean(Long id) {
        updateTableStatus(id, 0);
    }

    @Override
    public boolean checkoutTableIfSettled(Long id) {
        updateTableStatus(id, 3);
        return true;
    }

    @Override
        public void updateTableStatus(Long id, Integer status) {
            DiningTable table = diningTableMapper.selectById(id);
            if (table == null) {
                throw new BusinessException("Table not found");
            }
            int oldStatus = table.getStatus() == null ? -1 : table.getStatus();
            DiningTable update = new DiningTable();
            update.setId(id);
            update.setStatus(status);
            diningTableMapper.updateById(update);

            // 桌台状态变更推送到桌面看板
            Map<String, Object> data = Map.of(
                    "tableId", table.getId(),
                    "tableCode", table.getCode(),
                    "oldStatus", oldStatus,
                    "newStatus", status);
            wsService.broadcast(WsEventType.TABLE_STATUS, "/topic/table-status", data);
        }

    @Override
    public void releaseTable(Long id) {
        updateTableStatus(id, 0);
    }

    @Override
    public void downloadQrCode(Long id, HttpServletResponse response) {
        DiningTable table = diningTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException("Table not found");
        }
        byte[] png;
        if (StringUtils.hasText(table.getQrCodeUrl())) {
            png = resolveQrCodeBytes(table.getQrCodeUrl());
        } else {
            png = ensureTableQrCode(table);
        }

        String rawFileName = String.format("%s-%s-qrcode.png", table.getCode(), table.getName());
        String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        try {
            response.setContentType("image/png");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            response.getOutputStream().write(png);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Download table QR failed, tableId={}", id, e);
        }
    }

    @Override
    public int generateAllQrCodes() {
        List<DiningTable> tables = diningTableMapper.selectList(null);
        int generated = 0;
        for (DiningTable t : tables) {
            try {
                ensureTableQrCode(t);
                generated++;
            } catch (Exception e) {
                log.error("Generate table QR failed, code={}", t.getCode(), e);
            }
        }
        log.info("Batch generate table QR done: total={}, generated={}", tables.size(), generated);
        return generated;
    }

    @Override
    public QrCodeTaskVO submitGenerateAllQrCodesTask() {
        List<DiningTable> tables = diningTableMapper.selectList(null);
        QrCodeTaskVO vo = new QrCodeTaskVO();
        vo.setTaskId(newTaskId());
        vo.setTaskType("GENERATE_ALL");
        vo.setStatus("SUCCESS");
        vo.setMessage("二维码批量生成完成");
        int done = 0;
        for (DiningTable t : tables) {
            try {
                ensureTableQrCode(t);
                done++;
            } catch (Exception e) {
                log.error("Generate table QR failed, code={}", t.getCode(), e);
            }
        }
        vo.setTotal(tables.size());
        vo.setCompleted(done);
        vo.setDownloadable(false);
        vo.setCreateTime(LocalDateTime.now());
        vo.setFinishTime(LocalDateTime.now());
        QR_TASKS.put(vo.getTaskId(), vo);
        return vo;
    }

    @Override
    public QrCodeTaskVO submitDownloadAllQrCodesTask() {
        List<DiningTable> tables = diningTableMapper.selectList(null);
        QrCodeTaskVO vo = new QrCodeTaskVO();
        vo.setTaskId(newTaskId());
        vo.setTaskType("DOWNLOAD_ALL");
        vo.setStatus("SUCCESS");
        vo.setMessage("二维码压缩包已生成");
        vo.setTotal(tables.size());
        vo.setCompleted(tables.size());
        vo.setDownloadable(true);
        vo.setFileName("tables-qrcodes.zip");
        vo.setCreateTime(LocalDateTime.now());
        vo.setFinishTime(LocalDateTime.now());

        byte[] zipBytes = buildAreaGroupedZip(tables);
        QR_TASK_FILES.put(vo.getTaskId(), zipBytes);
        QR_TASKS.put(vo.getTaskId(), vo);
        return vo;
    }

    @Override
    public QrCodeTaskVO getQrCodeTask(String taskId) {
        QrCodeTaskVO vo = QR_TASKS.get(taskId);
        if (vo != null) {
            return vo;
        }
        // Fallback so the frontend polling terminates instead of hanging forever.
        QrCodeTaskVO fallback = new QrCodeTaskVO();
        fallback.setTaskId(taskId);
        fallback.setStatus("FAILED");
        fallback.setMessage("任务不存在或已过期");
        return fallback;
    }

    @Override
    public void downloadQrCodeTaskFile(String taskId, HttpServletResponse response) {
        byte[] bytes = QR_TASK_FILES.get(taskId);
        if (bytes == null) {
            try {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("QR archive not found or expired");
            } catch (Exception ignored) {
                // ignore
            }
            return;
        }
        try {
            response.setContentType("application/zip");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=tables-qrcodes.zip");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Download QR archive failed, taskId={}", taskId, e);
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Generate a 300x300 PNG QR whose content is {@code code=<tableCode>}. */
    private byte[] generateTableQrCodeImage(String tableCode) {
        String qrContent = "code=" + tableCode;
        return QrCodeUtil.generatePng(qrContent, 300, 300);
    }

    /** Generate (if needed), upload to MinIO, persist the public URL, and return the PNG bytes. */
    private byte[] ensureTableQrCode(DiningTable table) {
        byte[] png = generateTableQrCodeImage(table.getCode());
        String objectKey = QR_PREFIX + table.getCode() + ".png";
        String url = uploadToMinio(png, objectKey, "image/png");

        DiningTable update = new DiningTable();
        update.setId(table.getId());
        update.setQrCodeUrl(url);
        diningTableMapper.updateById(update);
        return png;
    }

    /** Upload PNG bytes to the public MinIO bucket and return the full access URL. */
    private String uploadToMinio(byte[] bytes, String objectKey, String contentType) {
        String bucket = minioConfig.getBucket();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("Upload QR to MinIO failed: " + e.getMessage());
        }
        return minioConfig.getEndpoint() + "/" + bucket + "/" + objectKey;
    }

    /** Download PNG bytes back from MinIO using a stored URL. */
    private byte[] resolveQrCodeBytes(String url) {
        String bucket = minioConfig.getBucket();
        String prefix = minioConfig.getEndpoint() + "/" + bucket + "/";
        String objectKey = url.startsWith(prefix) ? url.substring(prefix.length()) : url;
        try (var stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket).object(objectKey).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.warn("Read QR from MinIO failed, falls back to regenerate: {}", e.getMessage());
            return new byte[0];
        }
    }

    /** Pack all table QRs into a zip grouped by area folder (Client: e.g. "A区/"). */
    private byte[] buildAreaGroupedZip(List<DiningTable> tables) {
        Map<String, List<DiningTable>> byArea = tables.stream()
                .collect(Collectors.groupingBy(t -> StringUtils.hasText(t.getAreaName()) ? t.getAreaName() : "未分区"));
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                for (Map.Entry<String, List<DiningTable>> entry : byArea.entrySet()) {
                    String folder = sanitizeFileName(entry.getKey());
                    for (DiningTable t : entry.getValue()) {
                        byte[] png = StringUtils.hasText(t.getQrCodeUrl())
                                ? resolveQrCodeBytes(t.getQrCodeUrl())
                                : ensureTableQrCode(t);
                        zos.putNextEntry(new ZipEntry(folder + "/" + sanitizeFileName(t.getCode()) + ".png"));
                        zos.write(png);
                        zos.closeEntry();
                    }
                }
            }
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Build table QR zip failed", e);
            throw new BusinessException("打包桌台二维码失败");
        }
    }

    private String sanitizeFileName(String name) {
        return (name == null ? "" : name).replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String newTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private DiningTableVO toVO(DiningTable table) {
        DiningTableVO vo = new DiningTableVO();
        BeanUtils.copyProperties(table, vo);
        return vo;
    }
}