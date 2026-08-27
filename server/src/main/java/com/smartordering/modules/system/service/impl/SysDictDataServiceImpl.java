package com.smartordering.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.system.dto.DictDataCreateDTO;
import com.smartordering.modules.system.dto.DictDataUpdateDTO;
import com.smartordering.modules.system.entity.SysDictData;
import com.smartordering.modules.system.entity.SysDictType;
import com.smartordering.modules.system.mapper.SysDictDataMapper;
import com.smartordering.modules.system.mapper.SysDictTypeMapper;
import com.smartordering.modules.system.service.SysDictDataService;
import com.smartordering.modules.system.vo.DictDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dict data service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl implements SysDictDataService {

    private final SysDictDataMapper dictDataMapper;
    private final SysDictTypeMapper dictTypeMapper;

    @Override
    public Long create(DictDataCreateDTO dto) {
        if (dto.getTypeId() == null || !StringUtils.hasText(dto.getLabel())) {
            throw new BusinessException("typeId and label are required");
        }
        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(dto, data);
        data.setId(null);
        if (data.getStatus() == null) {
            data.setStatus(1);
        }
        if (data.getOrderNum() == null) {
            data.setOrderNum(0);
        }
        dictDataMapper.insert(data);
        return data.getId();
    }

    @Override
    public void update(DictDataUpdateDTO dto) {
        if (dto.getId() == null || dictDataMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Dict data not found");
        }
        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(dto, data);
        dictDataMapper.updateById(data);
    }

    @Override
    public void delete(Long dictDataId) {
        if (dictDataMapper.selectById(dictDataId) == null) {
            throw new BusinessException("Dict data not found");
        }
        dictDataMapper.deleteById(dictDataId);
    }

    @Override
    public List<DictDataVO> getByTypeId(Long typeId) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getTypeId, typeId)
                        .orderByAsc(SysDictData::getOrderNum))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DictDataVO> getByTypeCode(String typeCode) {
        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, typeCode));
        if (type == null) {
            return List.of();
        }
        return getByTypeId(type.getId());
    }

    @Override
    public DictDataVO getInfo(Long dictDataId) {
        SysDictData data = dictDataMapper.selectById(dictDataId);
        if (data == null) {
            throw new BusinessException("Dict data not found");
        }
        return toVO(data);
    }

    private DictDataVO toVO(SysDictData data) {
        DictDataVO vo = new DictDataVO();
        BeanUtils.copyProperties(data, vo);
        return vo;
    }
}