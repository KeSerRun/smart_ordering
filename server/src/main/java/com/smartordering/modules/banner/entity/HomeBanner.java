package com.smartordering.modules.banner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Home banner entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("home_banner")
public class HomeBanner extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private String subtitle;
    private String imageUrl;
    private Integer actionType;
    private String targetPath;
    private String scene;
    private Integer sort;
    private Integer status;
}