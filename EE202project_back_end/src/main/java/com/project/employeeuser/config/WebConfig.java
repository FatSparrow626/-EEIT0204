package com.project.employeeuser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload.avatar.path:uploads/avatars}")
    private String avatarUploadPath;

    /**
     * 配置靜態資源處理器
     * 讓前端可以直接存取上傳的檔案
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 獲取當前工作目錄用於日誌記錄
        String workingDir = System.getProperty("user.dir");
        String uploadLocation = "file:./uploads/";
        
        logger.info("配置靜態資源處理器:");
        logger.info("  - 工作目錄: {}", workingDir);
        logger.info("  - 頭像上傳路徑配置: {}", avatarUploadPath);
        logger.info("  - 統一上傳檔案位置: {}", uploadLocation);
        logger.info("  - URL 處理模式: /uploads/**");
        
        // 統一使用一個處理器處理所有上傳檔案（最簡化配置）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);

        // 保留預設的靜態資源配置
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
        
        logger.info("靜態資源處理器配置完成");
    }

    /**
     * 配置 CORS (跨域請求)
     * 允許前端存取上傳的檔案
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置 CORS: /uploads/** -> http://localhost:5173");
        
        registry.addMapping("/uploads/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}