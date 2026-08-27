package com.smartordering.modules.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.payment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * Payment record mapper
 *
 * @author smartordering
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}