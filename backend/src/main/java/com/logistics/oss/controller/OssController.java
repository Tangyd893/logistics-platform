package com.logistics.oss.controller;

import com.logistics.common.dto.Result;
import com.logistics.oss.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/oss")
@Tag(name = "文件管理")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','DISPATCHER')")
public class OssController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到 MinIO，返回访问 URL（有效期 7 天）")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", required = false, defaultValue = "general") String prefix) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        // 10MB 限制
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail("文件大小不能超过 10MB");
        }
        String url = fileUploadService.upload(file, prefix);
        return Result.ok(url);
    }

    @DeleteMapping
    @Operation(summary = "删除文件", description = "根据 MinIO 对象名删除文件")
    public Result<?> delete(@RequestParam String objectName) {
        fileUploadService.delete(objectName);
        return Result.ok(null);
    }
}
