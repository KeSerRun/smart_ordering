package com.smartordering.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.DictTypeCreateDTO;
import com.smartordering.modules.system.dto.DictTypeQueryDTO;
import com.smartordering.modules.system.dto.DictTypeUpdateDTO;
import com.smartordering.modules.system.entity.SysDictType;
import com.smartordering.modules.system.mapper.SysDictTypeMapper;
import com.smartordering.modules.system.service.SysDictTypeService;
import com.smartordering.modules.system.vo.DictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dict type service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl implements SysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;

    @Override
    public Long create(DictTypeCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName()) || !StringUtils.hasText(dto.getCode())) {
            throw new BusinessException("Dict name and code are required");
        }
        if (dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getCode, dto.getCode())) > 0) {
            throw new BusinessException("Dict code already exists");
        }
        SysDictType type = new SysDictType();
        BeanUtils.copyProperties(dto, type);
        type.setId(null);
        if (type.getStatus() == null) {
            type.setStatus(1);
        }
        dictTypeMapper.insert(type);
        return type.getId();
    }

    @Override
    public void update(DictTypeUpdateDTO dto) {
        if (dto.getId() == null || dictTypeMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Dict type not found");
        }
        SysDictType type = new SysDictType();
        BeanUtils.copyProperties(dto, type);
        dictTypeMapper.updateById(type);
    }

    @Override
    public void delete(Long dictTypeId) {
        if (dictTypeMapper.selectById(dictTypeId) == null) {
            throw new BusinessException("Dict type not found");
        }
        dictTypeMapper.deleteById(dictTypeId);
    }

    @Override
    public PageResult<DictTypeVO> pageList(DictTypeQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), SysDictType::getName, dto.getName())
                .like(StringUtils.hasText(dto.getCode()), SysDictType::getCode, dto.getCode())
                .orderByAsc(SysDictType::getId);
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        dictTypeMapper.selectPage(page, wrapper);
        List<DictTypeVO> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public List<DictTypeVO> listAll() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>().orderByAsc(SysDictType::getId))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public DictTypeVO getInfo(Long dictTypeId) {
        SysDictType type = dictTypeMapper.selectById(dictTypeId);
        if (type == null) {
            throw new BusinessException("Dict type not found");
        }
        return toVO(type);
    }

    private DictTypeVO toVO(SysDictType type) {
        DictTypeVO vo = new DictTypeVO();
        BeanUtils.copyProperties(type, vo);
        return vo;
    }
}