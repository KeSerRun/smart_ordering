package com.smartordering.modules.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * Payment record entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Order ID */
    private Long orderId;

    /** Payment serial number */
    private String paymentNo;

    /** Third-party transaction number */
    private String thirdPartyNo;

    /** Payment method: 0=WeChat 1=Alipay 2=Cash */
    private Integer paymentMethod;

    /** Payment amount */
    private BigDecimal amount;

    /** Status: 0=pending 1=paid 2=refunded 3=failed */
    private Integer status;

    /** Payer openid */
    private String payerOpenid;

    /** Callback raw data */
    private String callbackData;
}