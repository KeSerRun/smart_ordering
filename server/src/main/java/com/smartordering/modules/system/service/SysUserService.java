package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.AdminResetPasswordDTO;
import com.smartordering.modules.system.dto.AdminUserUpdateDTO;
import com.smartordering.modules.system.dto.LoginDTO;
import com.smartordering.modules.system.dto.PasswordUpdateDTO;
import com.smartordering.modules.system.dto.RegisterDTO;
import com.smartordering.modules.system.dto.UserCreateDTO;
import com.smartordering.modules.system.dto.UserQueryDTO;
import com.smartordering.modules.system.dto.UserUpdateDTO;
import com.smartordering.modules.system.vo.LoginVO;
import com.smartordering.modules.system.vo.UserInfoVO;
import com.smartordering.modules.system.vo.UserVO;

import java.util.List;

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

    /** Admin: create user (BCrypt password + module grants) */
    Long createUser(UserCreateDTO dto);

    /** Admin: update user profile + module grants */
    void updateUser(AdminUserUpdateDTO dto);

    /** Admin: delete user (logic delete; admin / self protected) */
    void deleteUser(Long userId);

    /** Admin: replace a user's module grants (core/ops/sys/kitchen) */
    void updateUserModules(Long userId, List<String> modules);
}