package com.smartordering.modules.table.service;

import com.smartordering.modules.table.dto.TableCreateDTO;
import com.smartordering.modules.table.dto.TableUpdateDTO;
import com.smartordering.modules.table.vo.DiningTableVO;
import com.smartordering.modules.table.vo.QrCodeTaskVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * Dining table service interface.
 *
 * @author smartordering
 */
public interface DiningTableService {

    DiningTableVO getByCode(String code);

    List<DiningTableVO> list(Long areaId);

    // ===== admin =====

    List<DiningTableVO> listAll();

    Long createTable(TableCreateDTO dto);

    void updateTable(TableUpdateDTO dto);

    void deleteTable(Long id);

    void markClean(Long id);

    boolean checkoutTableIfSettled(Long id);

    void updateTableStatus(Long id, Integer status);

    void releaseTable(Long id);

    void downloadQrCode(Long id, HttpServletResponse response);

    /** 删除桌台二维码：移除 MinIO 对象并清空 qrCodeUrl（未生成时抛出业务异常） */
    void deleteTableQrCode(Long id);

    int generateAllQrCodes();

    QrCodeTaskVO submitGenerateAllQrCodesTask();

    /** 消费者异步执行完毕后回写任务状态（SUCCESS / FAILED） */
    void completeGenerateAllQrTask(String taskId, Integer total, Integer completed, String error);

    QrCodeTaskVO submitDownloadAllQrCodesTask();

    QrCodeTaskVO getQrCodeTask(String taskId);

    void downloadQrCodeTaskFile(String taskId, HttpServletResponse response);
}