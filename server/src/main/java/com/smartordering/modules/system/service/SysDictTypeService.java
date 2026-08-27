package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.DictTypeCreateDTO;
import com.smartordering.modules.system.dto.DictTypeQueryDTO;
import com.smartordering.modules.system.dto.DictTypeUpdateDTO;
import com.smartordering.modules.system.vo.DictTypeVO;

import java.util.List;

/**
 * Dict type service interface.
 *
 * @author smartordering
 */
public interface SysDictTypeService {

    Long create(DictTypeCreateDTO dto);

    void update(DictTypeUpdateDTO dto);

    void delete(Long dictTypeId);

    PageResult<DictTypeVO> pageList(DictTypeQueryDTO dto);

    List<DictTypeVO> listAll();

    DictTypeVO getInfo(Long dictTypeId);
}