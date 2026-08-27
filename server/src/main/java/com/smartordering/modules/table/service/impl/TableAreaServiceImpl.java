package com.smartordering.modules.table.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.table.dto.TableAreaCreateDTO;
import com.smartordering.modules.table.dto.TableAreaUpdateDTO;
import com.smartordering.modules.table.entity.TableArea;
import com.smartordering.modules.table.mapper.TableAreaMapper;
import com.smartordering.modules.table.service.TableAreaService;
import com.smartordering.modules.table.vo.TableAreaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Table area service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class TableAreaServiceImpl implements TableAreaService {

    private final TableAreaMapper tableAreaMapper;

    @Override
    public List<TableAreaVO> listAll() {
        return tableAreaMapper.selectList(new LambdaQueryWrapper<TableArea>()
                        .orderByAsc(TableArea::getSort).orderByAsc(TableArea::getId))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<TableAreaVO> listEnabled() {
        return tableAreaMapper.selectList(new LambdaQueryWrapper<TableArea>()
                        .eq(TableArea::getStatus, 1)
                        .orderByAsc(TableArea::getSort))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void createArea(TableAreaCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Area name is required");
        }
        TableArea area = new TableArea();
        BeanUtils.copyProperties(dto, area);
        area.setId(null);
        if (area.getStatus() == null) {
            area.setStatus(1);
        }
        if (area.getSort() == null) {
            area.setSort(0);
        }
        tableAreaMapper.insert(area);
    }

    @Override
    public void updateArea(TableAreaUpdateDTO dto) {
        if (dto.getId() == null || tableAreaMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Area not found");
        }
        TableArea area = new TableArea();
        BeanUtils.copyProperties(dto, area);
        tableAreaMapper.updateById(area);
    }

    @Override
    public void deleteArea(Long id) {
        if (tableAreaMapper.selectById(id) == null) {
            throw new BusinessException("Area not found");
        }
        tableAreaMapper.deleteById(id);
    }

    private TableAreaVO toVO(TableArea area) {
        TableAreaVO vo = new TableAreaVO();
        BeanUtils.copyProperties(area, vo);
        return vo;
    }
}