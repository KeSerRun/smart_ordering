package com.smartordering.modules.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.mq.dto.MqMessageQueryDTO;
import com.smartordering.modules.mq.entity.MqMessage;
import com.smartordering.modules.mq.mapper.MqMessageMapper;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.mq.vo.MqMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Reliable message service implementation
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class ReliableMessageServiceImpl implements ReliableMessageService {

    private final MqMessageMapper mqMessageMapper;

    @Override
    public IPage<MqMessageVO> pageMessages(MqMessageQueryDTO dto) {
        LambdaQueryWrapper<MqMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getBizType())) {
            wrapper.eq(MqMessage::getBizType, dto.getBizType());
        }
        if (StringUtils.hasText(dto.getMessageKey())) {
            wrapper.eq(MqMessage::getMessageKey, dto.getMessageKey());
        }
        if (dto.getDeliverStatus() != null) {
            wrapper.eq(MqMessage::getDeliverStatus, dto.getDeliverStatus());
        }
        wrapper.orderByDesc(MqMessage::getCreateTime);

        Page<MqMessage> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<MqMessage> result = mqMessageMapper.selectPage(page, wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public void retryMessage(Long id) {
        MqMessage message = mqMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("Message not found");
        }
        // Reset to pending delivery
        message.setDeliverStatus(0);
        message.setRetryCount(0);
        message.setNextRetryTime(LocalDateTime.now());
        message.setLastError(null);
        mqMessageMapper.updateById(message);
    }

    private MqMessageVO toVO(MqMessage message) {
        MqMessageVO vo = new MqMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}