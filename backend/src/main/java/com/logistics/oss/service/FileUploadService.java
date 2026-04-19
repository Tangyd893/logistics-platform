package com.logistics.oss.service;

import com.logistics.common.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket:logistics}")
    private String bucket;

    /**
     * 上传文件，返回访问 URL（7 天有效期）
     */
    public String upload(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "unknown";
        String ext = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx > 0) ext = originalFilename.substring(dotIdx);
        String objectName = (prefix != null ? prefix + "/" : "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;

        try {
            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            // 生成访问 URL（有效期 7 天）
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .method(Method.GET)
                            .build()
            );

            log.info("[OSS] 上传: {} -> {}", originalFilename, objectName);
            return url;

        } catch (IOException e) {
            log.error("[OSS] 上传失败: {}", originalFilename, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("[OSS] 上传失败: {}", originalFilename, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件（根据 objectName）
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("[OSS] 删除: {}", objectName);
        } catch (Exception e) {
            log.error("[OSS] 删除失败: {}", objectName, e);
        }
    }
}
