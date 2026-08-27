package com.smartordering.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.ConfigCreateDTO;
import com.smartordering.modules.system.dto.ConfigQueryDTO;
import com.smartordering.modules.system.dto.ConfigUpdateDTO;
import com.smartordering.modules.system.entity.SysConfig;
import com.smartordering.modules.system.mapper.SysConfigMapper;
import com.smartordering.modules.system.service.SysConfigService;
import com.smartordering.modules.system.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Config service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private static final String THEME_KEY = "admin.theme.preset";

    private final SysConfigMapper configMapper;

    @Override
    public Long create(ConfigCreateDTO dto) {
        if (!StringUtils.hasText(dto.getConfigKey())) {
            throw new BusinessException("configKey is required");
        }
        if (configMapper.selectCount(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, dto.getConfigKey())) > 0) {
            throw new BusinessException("Config key already exists");
        }
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(dto, config);
        config.setId(null);
        configMapper.insert(config);
        return config.getId();
    }

    @Override
    public void update(ConfigUpdateDTO dto) {
        if (dto.getId() == null || configMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Config not found");
        }
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(dto, config);
        configMapper.updateById(config);
    }

    @Override
    public void delete(Long configId) {
        if (configMapper.selectById(configId) == null) {
            throw new BusinessException("Config not found");
        }
        configMapper.deleteById(configId);
    }

    @Override
    public PageResult<ConfigVO> pageList(ConfigQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), SysConfig::getName, dto.getName())
                .like(StringUtils.hasText(dto.getConfigKey()), SysConfig::getConfigKey, dto.getConfigKey())
                .orderByAsc(SysConfig::getId);
        Page<SysConfig> page = new Page<>(pageNum, pageSize);
        configMapper.selectPage(page, wrapper);
        List<ConfigVO> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public String getByKey(String configKey) {
        SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public ConfigVO getInfo(Long configId) {
        SysConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("Config not found");
        }
        return toVO(config);
    }

    @Override
    public String getAdminThemePreset() {
        return getByKey(THEME_KEY);
    }

    @Override
    public void saveAdminThemePreset(String presetId) {
        SysConfig existing = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, THEME_KEY));
        if (existing == null) {
            SysConfig config = new SysConfig();
            config.setName("Admin theme preset");
            config.setConfigKey(THEME_KEY);
            config.setConfigValue(presetId);
            configMapper.insert(config);
        } else {
            SysConfig update = new SysConfig();
            update.setId(existing.getId());
            update.setConfigValue(presetId);
            configMapper.updateById(update);
        }
    }

    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}