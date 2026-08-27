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

    int generateAllQrCodes();

    QrCodeTaskVO submitGenerateAllQrCodesTask();

    QrCodeTaskVO submitDownloadAllQrCodesTask();

    QrCodeTaskVO getQrCodeTask(String taskId);

    void downloadQrCodeTaskFile(String taskId, HttpServletResponse response);
}