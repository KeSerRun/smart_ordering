package com.smartordering.modules.table.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Dining table view object
 *
 * @author smartordering
 */
@Data
public class DiningTableVO {

    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private Integer status;

    /** 二维码是否已生成（qrCodeUrl 非空） */
    private Boolean qrCodeGenerated;
    private String qrCodeUrl;
    private Long areaId;
    private String areaName;
    private String currentSessionCode;
    private LocalDateTime createTime;
}