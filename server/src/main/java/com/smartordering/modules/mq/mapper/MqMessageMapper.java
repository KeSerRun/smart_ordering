package com.smartordering.modules.mq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.mq.entity.MqMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * MQ message mapper
 *
 * @author smartordering
 */
@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {
}