package com.smartordering.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-模块权限关联实体
 *
 * <p>模块编码（与前端侧边栏分组一致）：
 * core=点餐核心（菜品/桌台/订单）、ops=运营管理、sys=系统管理、kitchen=后厨任务。</p>
 *
 * @author smartordering
 */
@Data
@TableName("sys_user_module")
public class SysUserModule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long userId;

    private String moduleCode;

    private LocalDateTime createTime;
}