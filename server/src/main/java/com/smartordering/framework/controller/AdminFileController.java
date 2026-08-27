package com.smartordering.framework.controller;

import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.common.vo.FileUploadResultVO;
import com.smartordering.framework.config.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

/**
 * File upload controller (MinIO backing store).
 *
 * @author smartordering
 */
@Tag(name = "File (Upload)")
@RestController
@RequestMapping("/admin/file")
@RequiredArgsConstructor
public class AdminFileController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Operation(summary = "Upload a dish image")
    @PostMapping("/upload/dish-image")
    public ApiResponse<FileUploadResultVO> uploadDishImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(uploadImage(file, "dish/"));
    }

    @Operation(summary = "Upload a banner image")
    @PostMapping("/upload/banner-image")
    public ApiResponse<FileUploadResultVO> uploadBannerImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(uploadImage(file, "banner/"));
    }

    private FileUploadResultVO uploadImage(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is empty");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BusinessException("Only image files are allowed");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("Image size must not exceed 5MB");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        String objectName = prefix + UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            String bucket = minioConfig.getBucket();
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            // Allow anonymous read so image URLs work straight from the browser
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                    + "\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],"
                    + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(policy)
                    .build());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new BusinessException("File upload failed: " + e.getMessage());
        }

        String url = minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName;
        return new FileUploadResultVO(url, objectName);
    }

    private String resolveExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        String ext = filename.substring(filename.lastIndexOf('.'));
        return ext.toLowerCase(Locale.ROOT);
    }
}