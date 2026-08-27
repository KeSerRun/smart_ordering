package com.smartordering.modules.table.service;

import com.smartordering.modules.table.dto.TableAreaCreateDTO;
import com.smartordering.modules.table.dto.TableAreaUpdateDTO;
import com.smartordering.modules.table.vo.TableAreaVO;

import java.util.List;

/**
 * Table area service interface.
 *
 * @author smartordering
 */
public interface TableAreaService {

    List<TableAreaVO> listAll();

    List<TableAreaVO> listEnabled();

    void createArea(TableAreaCreateDTO dto);

    void updateArea(TableAreaUpdateDTO dto);

    void deleteArea(Long id);
}