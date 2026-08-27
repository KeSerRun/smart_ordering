package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.AdminResetPasswordDTO;
import com.smartordering.modules.system.dto.LoginDTO;
import com.smartordering.modules.system.dto.PasswordUpdateDTO;
import com.smartordering.modules.system.dto.RegisterDTO;
import com.smartordering.modules.system.dto.UserQueryDTO;
import com.smartordering.modules.system.dto.UserUpdateDTO;
import com.smartordering.modules.system.vo.LoginVO;
import com.smartordering.modules.system.vo.UserInfoVO;
import com.smartordering.modules.system.vo.UserVO;

/**
 * User service interface.
 *
 * @author smartordering
 */
public interface SysUserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserInfoVO getCurrentUserInfo();

    /** Admin: paged user list */
    PageResult<UserVO> pageList(UserQueryDTO dto);

    /** Admin: update current user's profile */
    void updateUserInfo(UserUpdateDTO dto);

    /** Admin: change own password */
    void updatePassword(PasswordUpdateDTO dto);

    /** Admin: reset a user's password (kick + clear cache) */
    void resetPassword(Long userId, AdminResetPasswordDTO dto);

    /** Admin: enable/disable a user (cannot disable self) */
    void updateStatus(Long userId, Integer status);
}