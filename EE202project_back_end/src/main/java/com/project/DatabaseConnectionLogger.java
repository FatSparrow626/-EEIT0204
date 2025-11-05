
package com.project;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 應用程式啟動後，檢查並記錄資料庫連線狀態。
 * ApplicationRunner 會在 Spring Boot 應用程式完全啟動後執行 run 方法。
 */
@Component
public class DatabaseConnectionLogger implements ApplicationRunner {

    // 建立一個 Logger 實例，用於記錄日誌
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionLogger.class);

    // 從 Spring 容器中自動注入配置好的 DataSource
    @Autowired
    private DataSource dataSource;

    // 從 application.properties 中直接注入乾淨的連線 URL
    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("正在檢查資料庫連線狀態...");
        try (Connection connection = dataSource.getConnection()) {
            // 檢查連線是否成功建立且未關閉
            if (connection != null && !connection.isClosed()) {
                logger.info("============================================================");
                logger.info("資料庫連線成功！");
                logger.info("連線位址: {}", databaseUrl);
                logger.info("============================================================");
            }
        } catch (Exception e) {
            // 如果連線失敗，則記錄詳細的錯誤訊息
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.error("資料庫連線失敗！請檢查 application.properties 中的設定。", e);
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}

