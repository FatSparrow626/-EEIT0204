package com.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot 應用程式主入口點
 *
 * @SpringBootApplication 是一個方便的註解，它包含了：
 *                        - @Configuration: 標記該類別為應用程式上下文的 Bean 定義來源。
 *                        - @EnableAutoConfiguration: 告訴 Spring Boot 開始根據
 *                        classpath 設定、其他 bean 和各種屬性設定來加入 bean。
 *                        - @ComponentScan: 告訴 Spring 在這個套件 (com.project)
 *                        及其子套件中尋找其他元件、設定和服務。
 *
 *                        我們使用 scanBasePackages 屬性來明確指定 Spring 要掃描的套件，
 *                        因為我們的模組 (supplier, depot, workorder, core)
 *                        分佈在不同的基礎套件下。
 */
@SpringBootApplication(scanBasePackages = { "com.project" })
@EnableJpaRepositories(basePackages = { "com.project" })
// ▼▼▼ 這裡保持上次的修改 ▼▼▼
@EntityScan(basePackages = { "com.project" })
@EnableScheduling // 啟用 Spring 排程功能
public class WarehouseManagementApplication {
    /**
     * 應用程式的主方法，用於啟動 Spring Boot 應用程式。
     * * @param args 命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

}
