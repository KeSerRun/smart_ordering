package com.smartordering.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.LoginLogQueryDTO;
import com.smartordering.modules.system.dto.OperationLogQueryDTO;
import com.smartordering.modules.system.entity.SysLoginLog;
import com.smartordering.modules.system.entity.SysOperationLog;
import com.smartordering.modules.system.mapper.SysLoginLogMapper;
import com.smartordering.modules.system.mapper.SysOperationLogMapper;
import com.smartordering.modules.system.service.SysLogService;
import com.smartordering.modules.system.vo.LoginLogVO;
import com.smartordering.modules.system.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sys log query service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl implements SysLogService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysOperationLogMapper operationLogMapper;

    @Override
    public PageResult<LoginLogVO> pageLoginLogs(LoginLogQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getUsername()), SysLoginLog::getUsername, dto.getUsername())
                .eq(dto.getStatus() != null, SysLoginLog::getStatus, dto.getStatus())
                .ge(dto.getStartTime() != null, SysLoginLog::getLoginTime, dto.getStartTime())
                .le(dto.getEndTime() != null, SysLoginLog::getLoginTime, dto.getEndTime())
                .orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> page = new Page<>(pageNum, pageSize);
        loginLogMapper.selectPage(page, wrapper);
        List<LoginLogVO> list = page.getRecords().stream().map(l -> {
            LoginLogVO vo = new LoginLogVO();
            BeanUtils.copyProperties(l, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public PageResult<OperationLogVO> pageOperationLogs(OperationLogQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getUsername()), SysOperationLog::getUsername, dto.getUsername())
                .like(StringUtils.hasText(dto.getModule()), SysOperationLog::getModule, dto.getModule())
                .eq(dto.getStatus() != null, SysOperationLog::getStatus, dto.getStatus())
                .ge(dto.getStartTime() != null, SysOperationLog::getCreateTime, dto.getStartTime())
                .le(dto.getEndTime() != null, SysOperationLog::getCreateTime, dto.getEndTime())
                .orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        operationLogMapper.selectPage(page, wrapper);
        List<OperationLogVO> list = page.getRecords().stream().map(l -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtils.copyProperties(l, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public void recordLogin(String username, String ip, String browser, String os, int status, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setLocation(null);
        log.setBrowser(browser);
        log.setOs(os);
        log.setStatus(status);
        log.setMessage(StringUtils.hasText(message) && message.length() > 500 ? message.substring(0, 500) : message);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    @Override
    public void recordOperation(SysOperationLog record) {
        if (record == null) {
            return;
        }
        operationLogMapper.insert(record);
    }
}