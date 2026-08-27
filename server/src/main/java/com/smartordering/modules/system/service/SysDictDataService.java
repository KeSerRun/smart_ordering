package com.smartordering.modules.system.service;

import com.smartordering.modules.system.dto.DictDataCreateDTO;
import com.smartordering.modules.system.dto.DictDataUpdateDTO;
import com.smartordering.modules.system.vo.DictDataVO;

import java.util.List;

/**
 * Dict data service interface.
 *
 * @author smartordering
 */
public interface SysDictDataService {

    Long create(DictDataCreateDTO dto);

    void update(DictDataUpdateDTO dto);

    void delete(Long dictDataId);

    List<DictDataVO> getByTypeId(Long typeId);

    List<DictDataVO> getByTypeCode(String typeCode);

    DictDataVO getInfo(Long dictDataId);
}