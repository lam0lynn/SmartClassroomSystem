package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根路径
        String uploadPath = "file:" + System.getProperty("user.dir") + File.separator + "upload" + File.separator;
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);
    }
} 