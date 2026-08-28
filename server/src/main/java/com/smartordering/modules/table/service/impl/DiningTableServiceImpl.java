package com.smartordering.modules.table.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.smartordering.common.enums.WsEventType;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.framework.config.MinioConfig;
import com.smartordering.framework.config.RabbitMqConfig;
import com.smartordering.framework.websocket.WsService;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.table.dto.TableCreateDTO;
import com.smartordering.modules.table.dto.TableQrCodeEvent;
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
import io.minio.RemoveObjectArgs;
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
        private final ReliableMessageService reliableMessageService;
        private final RabbitMqConfig rabbitMqConfig;

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
        byte[] png = resolveOrRegenerate(table);

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
            int skipped = 0;
            for (DiningTable t : tables) {
                if (StringUtils.hasText(t.getQrCodeUrl())) {
                    // 已有二维码：跳过，不重新生成覆盖（「生成全部」只补缺失的）
                    skipped++;
                    continue;
                }
                try {
                    ensureTableQrCode(t);
                    generated++;
                } catch (Exception e) {
                    log.error("Generate table QR failed, code={}", t.getCode(), e);
                }
            }
            log.info("Batch generate table QR done: total={}, generated={}, skipped={}",
                    tables.size(), generated, skipped);
            return generated;
        }

    @Override
        public QrCodeTaskVO submitGenerateAllQrCodesTask() {
            // 1. 先登记任务（PENDING），请求立即返回，前端轮询状态
            List<DiningTable> tables = diningTableMapper.selectList(null);
            String taskId = newTaskId();
            QrCodeTaskVO vo = new QrCodeTaskVO();
            vo.setTaskId(taskId);
            vo.setTaskType("GENERATE_ALL");
            vo.setStatus("PENDING");
            vo.setMessage("二维码批量生成任务已提交，处理中");
            vo.setTotal(tables.size());
            vo.setCompleted(0);
            vo.setDownloadable(false);
            vo.setCreateTime(LocalDateTime.now());
            QR_TASKS.put(taskId, vo);

            // 2. 可靠消息：写 mq_message 发件箱后异步投递（routing key: table.qrcode.generate），
            //    RabbitMQ 暂时不可用时消息留在发件箱，由 ReliableMessageResendTask 定时补偿重发
            TableQrCodeEvent event = TableQrCodeEvent.builder()
                    .messageKey(taskId)
                    .taskId(taskId)
                    .total(tables.size())
                    .build();
            reliableMessageService.send(taskId, rabbitMqConfig.getTableQrCodeRoutingKey(),
                    "GEN_ALL_QR", "TABLE_QR", taskId, event);
            log.info("Table QR generate task submitted: taskId={}, total={}", taskId, tables.size());
            return vo;
        }

        @Override
        public void completeGenerateAllQrTask(String taskId, Integer total, Integer completed, String error) {
            QrCodeTaskVO vo = QR_TASKS.get(taskId);
            if (vo == null) {
                // 任务登记已丢失（如服务重启），补一个完整 VO 保证前端轮询能收敛
                vo = new QrCodeTaskVO();
                vo.setTaskId(taskId);
                vo.setTaskType("GENERATE_ALL");
                vo.setCreateTime(LocalDateTime.now());
            }
            boolean failed = StringUtils.hasText(error);
            vo.setStatus(failed ? "FAILED" : "SUCCESS");
            vo.setMessage(failed ? "生成失败：" + error : "二维码批量生成完成");
            if (total != null) {
                vo.setTotal(total);
            }
            vo.setCompleted(completed == null ? 0 : completed);
            vo.setDownloadable(false);
            vo.setFinishTime(LocalDateTime.now());
            QR_TASKS.put(taskId, vo);
            log.info("Table QR generate task finished: taskId={}, status={}, total={}, completed={}",
                    taskId, vo.getStatus(), vo.getTotal(), vo.getCompleted());
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

        byte[] zipBytes = buildAllQrZip(tables);
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

    /** 读回二维码 PNG；MinIO 对象缺失/读失败时现场重新生成（懒重建兜底，避免下载 0 字节损坏文件） */
        private byte[] resolveOrRegenerate(DiningTable table) {
            if (StringUtils.hasText(table.getQrCodeUrl())) {
                byte[] existing = resolveQrCodeBytes(table.getQrCodeUrl());
                if (existing.length > 0) {
                    return existing;
                }
                log.warn("QR object missing/empty in MinIO, regenerate: code={}, url={}",
                        table.getCode(), table.getQrCodeUrl());
            }
            return ensureTableQrCode(table);
        }

        /**
         * 生成或复用桌台二维码 PNG：已有 URL 且 MinIO 可读则直接返回现成的（不重新生成、不覆盖上传）；
         * 否则生成 300x300 PNG、上传 MinIO、持久化公开 URL 后返回字节。
         */
        private byte[] ensureTableQrCode(DiningTable table) {
            if (StringUtils.hasText(table.getQrCodeUrl())) {
                byte[] existing = resolveQrCodeBytes(table.getQrCodeUrl());
                if (existing.length > 0) {
                    return existing; // 已有：跳过重新生成
                }
                log.warn("QR object missing in MinIO, will regenerate: code={}", table.getCode());
            }
            byte[] png = generateTableQrCodeImage(table.getCode());
            String objectKey = QR_PREFIX + table.getCode() + ".png";
            String url = uploadToMinio(png, objectKey, "image/png");

            DiningTable update = new DiningTable();
            update.setId(table.getId());
            update.setQrCodeUrl(url);
            diningTableMapper.updateById(update);
            return png;
        }

        @Override
        public void deleteTableQrCode(Long id) {
            DiningTable table = diningTableMapper.selectById(id);
            if (table == null) {
                throw new BusinessException("Table not found");
            }
            if (!StringUtils.hasText(table.getQrCodeUrl())) {
                throw new BusinessException("该桌台暂无二维码，无需删除");
            }
            // 删除 MinIO 对象（尽力而为：对象已丢失时忽略，不影响清库）
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(extractObjectKey(table.getQrCodeUrl()))
                        .build());
            } catch (Exception e) {
                log.warn("Remove QR object from MinIO failed, ignore: code={}, url={}",
                        table.getCode(), table.getQrCodeUrl(), e);
            }
            // 清空落库字段（MyBatis-Plus NOT_NULL 策略下 updateById 不写 null，必须用 UpdateWrapper.set）
            diningTableMapper.update(null, new LambdaUpdateWrapper<DiningTable>()
                    .eq(DiningTable::getId, id)
                    .set(DiningTable::getQrCodeUrl, null));
            log.info("Table QR deleted: tableId={}, code={}", id, table.getCode());
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
            String objectKey = extractObjectKey(url);
            try (var stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket).object(objectKey).build())) {
                return stream.readAllBytes();
            } catch (Exception e) {
                log.warn("Read QR from MinIO failed, falls back to regenerate: {}", e.getMessage());
                return new byte[0];
            }
        }

        /** 从完整 URL 提取 MinIO object key（如 http://host/bucket/table/qrcode/A01.png -> table/qrcode/A01.png） */
        private String extractObjectKey(String url) {
            String prefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/";
            return url != null && url.startsWith(prefix) ? url.substring(prefix.length()) : url;
        }

    /** Pack all table QRs into a zip, each file named {@code 名称-代码-桌区.png}. */
        private byte[] buildAllQrZip(List<DiningTable> tables) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                    for (DiningTable t : tables) {
                        byte[] png = resolveOrRegenerate(t);
                        String area = StringUtils.hasText(t.getAreaName()) ? t.getAreaName() : "未分区";
                        String filename = String.format("%s-%s-%s.png",
                                sanitizeFileName(t.getName()), sanitizeFileName(t.getCode()), sanitizeFileName(area));
                        zos.putNextEntry(new ZipEntry(filename));
                        zos.write(png);
                        zos.closeEntry();
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
            vo.setQrCodeGenerated(StringUtils.hasText(table.getQrCodeUrl()));
            return vo;
        }
    }