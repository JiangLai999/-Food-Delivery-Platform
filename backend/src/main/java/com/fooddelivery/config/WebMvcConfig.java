package com.fooddelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置，用于暴露本地图片存储目录
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用项目根目录下的 storage/images，兼容不同电脑
        String userDir = System.getProperty("user.dir");
        String storagePath = userDir + File.separator + "storage" + File.separator + "images" + File.separator;
        
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
            System.out.println("创建图片存储目录：" + storagePath);
        }
        
        if (storageDir.exists() && storageDir.isDirectory()) {
            System.out.println("静态资源目录：" + storagePath);
            
            // Windows需要使用 file:/C:/ 格式，Linux/Mac使用 file:///
            String resourceLocation;
            if (File.separator.equals("\\")) {
                // Windows
                resourceLocation = "file:/" + storagePath.replace("\\", "/");
            } else {
                // Linux/Mac
                resourceLocation = "file://" + storagePath;
            }
            
            // 映射 /api/images/** (Spring Boot context-path为/api)
            registry.addResourceHandler("/api/images/**")
                    .addResourceLocations(resourceLocation)
                    .setCachePeriod(3600);
            
            // 同时映射 /images/** (兼容不带api前缀的请求)
            registry.addResourceHandler("/images/**")
                    .addResourceLocations(resourceLocation)
                    .setCachePeriod(3600);
            
            System.out.println("映射配置：/images/** 和 /api/images/** -> " + resourceLocation);
        } else {
            System.out.println("错误：静态资源目录不存在：" + storagePath);
        }
    }
}
