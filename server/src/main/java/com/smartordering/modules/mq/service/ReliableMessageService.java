package com.smartordering.modules.mq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartordering.modules.mq.dto.MqMessageQueryDTO;
import com.smartordering.modules.mq.vo.MqMessageVO;

/**
 * Reliable message service interface
 *
 * @author smartordering
 */
public interface ReliableMessageService {

    IPage<MqMessageVO> pageMessages(MqMessageQueryDTO dto);

    void retryMessage(Long id);
}