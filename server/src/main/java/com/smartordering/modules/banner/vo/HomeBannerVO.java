package com.smartordering.modules.banner.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Home banner view object
 *
 * @author smartordering
 */
@Data
public class HomeBannerVO {

    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private Integer actionType;
    private String targetPath;
    private String scene;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}