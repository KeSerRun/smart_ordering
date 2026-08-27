package com.smartordering.modules.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response VO 
 *
 * @author smartordering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
    private String tokenName;
    private UserInfoVO userInfo;
}