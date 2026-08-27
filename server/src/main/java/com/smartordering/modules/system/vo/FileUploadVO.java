package com.smartordering.modules.system.vo;

import lombok.Data;

/**
 * 文件上传结果
 */
@Data
public class FileUploadVO {

    /**
     * 文件访问地址
     */
    private String url;

    /**
     * 对象名
     */
    private String objectName;
}
