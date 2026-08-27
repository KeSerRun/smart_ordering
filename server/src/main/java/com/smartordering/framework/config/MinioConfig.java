package com.smartordering.framework.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO client configuration.
 *
 * <p>Reads connection settings from the {@code minio.*} keys in
 * {@code application-dev.yml} (endpoint / access-key / secret-key / bucket).
 * On application startup the bucket is created (if missing) and set to public-read
 * so object URLs work directly from the browser.</p>
 *
 * @author smartordering
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Ensure the bucket exists and is publicly readable once the app is up.
     * Failure is non-fatal: MinIO may be offline at boot time, in which case the
     * upload endpoint re-applies the same policy lazily on first upload.
     */
    @Bean
    public ApplicationRunner minioBucketInitializer(MinioClient minioClient) {
        return args -> {
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("MinIO bucket created: {}", bucket);
                }
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(publicReadPolicy(bucket))
                        .build());
                log.info("MinIO bucket '{}' is publicly readable", bucket);
            } catch (Exception e) {
                log.warn("MinIO bucket init skipped: {}", e.getMessage());
            }
        };
    }

    private String publicReadPolicy(String bucket) {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBucket() {
        return bucket;
    }
}