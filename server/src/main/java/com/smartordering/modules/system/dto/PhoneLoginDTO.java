package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 小程序手机号登录请求 DTO
 *


 */
@Data
public class PhoneLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 微信 wx.login 返回的临时凭证 */
    @NotBlank(message = "code不能为空")
    private String code;

    /** 微信 getPhoneNumber 返回的 phoneCode */
    @NotBlank(message = "phoneCode不能为空")
    private String phoneCode;
}
