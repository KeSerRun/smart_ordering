package com.smartordering.modules.system.vo;

import lombok.Data;

import java.util.List;

/**
 * Current user info VO
 *
 * @author smartordering
 */
@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
}