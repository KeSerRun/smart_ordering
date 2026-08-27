package com.smartordering.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File upload result returned to the frontend.
 *
 * @author smartordering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResultVO {

    /** Publicly accessible URL */
    private String url;

    /** Object key inside the bucket */
    private String objectName;
}