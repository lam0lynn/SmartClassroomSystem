package org.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
@CrossOrigin
@RestController
@RequestMapping("/img")
public class UploadController {

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件为空");
            return result;
        }
        try {
            // 存到项目根目录下的 upload 文件夹
            String uploadDir = System.getProperty("user.dir") + File.separator + "upload" + File.separator;
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + suffix;

            File dest = new File(uploadDir + newFileName);
            file.transferTo(dest);

            result.put("success", true);
            result.put("url", "/images/" + newFileName);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
            return result;
        }
    }

    public static class UploadResult {
        public String url;
        public UploadResult(String url) { this.url = url; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
} 