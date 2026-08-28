package com.smartordering.modules.mq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.mq.entity.MqConsumeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MQ 消费日志 Mapper
 *
 * @author smartordering
 */
@Mapper
public interface MqConsumeLogMapper extends BaseMapper<MqConsumeLog> {
}