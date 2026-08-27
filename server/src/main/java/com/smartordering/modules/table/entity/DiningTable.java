package com.smartordering.modules.table.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Dining table entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dining_table")
public class DiningTable extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Table code (linked to QR code) */
    private String code;

    /** Table name (e.g. "A1桌") */
    private String name;

    /** Seat capacity */
    private Integer capacity;

    /** Status: 0=idle 1=occupied 2=settled 3=to clean */
    private Integer status;

    /** QR code image URL */
    private String qrCodeUrl;

    /** Area ID */
    private Long areaId;

    /** Area name (e.g. "大厅", "包间") */
    private String areaName;

    /** Current session code */
    private String currentSessionCode;
}