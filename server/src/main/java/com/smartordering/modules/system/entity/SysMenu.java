package com.smartordering.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * System menu entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Parent menu ID */
    private Long parentId;

    /** Menu name */
    private String name;

    /** Route path */
    private String path;

    /** Component path */
    private String component;

    /** Permission code (e.g. system:user:list) */
    private String permission;

    /** Type: 0=directory 1=menu 2=button */
    private Integer type;

    /** Icon */
    private String icon;

    /** Sort order */
    private Integer orderNum;

    /** Status: 0=disabled 1=enabled */
    private Integer status;
}