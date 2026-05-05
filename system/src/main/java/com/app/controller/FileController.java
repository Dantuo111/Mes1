package com.app.controller;

import com.app.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * 上传图片文件
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        // 校验文件格式
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.error("只允许上传图片文件");
        }

        // 使用UUID重命名文件
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // 确保上传目录存在
        String userDir = System.getProperty("user.dir");
        File uploadDir = new File(userDir, uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 保存文件
        try {
            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);
        } catch (IOException e) {
            return Result.error("文件上传失败");
        }

        // 返回文件访问路径
        String fileUrl = "/uploads/" + newFilename;
        return Result.success(fileUrl);
    }
}
