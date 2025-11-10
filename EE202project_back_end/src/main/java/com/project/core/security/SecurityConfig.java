package com.project.core.security;

import com.project.core.security.jwt.AuthEntryPointJwt; // AuthEntryPointJwt：處理未認證請求的入口點
import com.project.core.security.jwt.AuthTokenFilter; //AuthTokenFilter：JWT Token 驗證過濾器
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; //：HTTP 方法枚舉 (GET, POST, PUT, DELETE 等)
import org.springframework.security.authentication.AuthenticationManager; //AuthenticationManager：認證管理器
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // DaoAuthenticationProvider：基於資料庫的認證提供者
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // 用途：會話管理政策，用於設定無狀態 (STATELESS)
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/** 這個控制器是整個認證流程的入口點，它串接了SecurityConfig和EmployeeUser模型。 */

/*
 * 1. 啟動時：SecurityConfig 組裝所有元件
 * 2. 請求進來：AuthTokenFilter 先檢查 JWT token
 * 3. Token 驗證：JwtUtils 檢查 token 有效性
 * 4. 載入使用者：EmployeeUserDetailsService 查詢完整資料
 * 5. 資料包裝：EmployeeUserDetails 提供標準介面
 * 6. 權限檢查：SecurityConfig 的規則判斷是否允許
 * 7. 錯誤處理：AuthEntryPointJwt 處理失敗情況
 */

@Configuration // 標記為 Spring 配置類別，會被 Spring 容器掃描和管理
@EnableWebSecurity // 啟用 Spring Security 預設的「安檢流水線
                   // (SecurityFilterChain)」、這個流水線上預設的「安檢站(UsernamePasswordAuthenticationFilter)」
@EnableMethodSecurity // 啟用方法級別的安全性註解，允許在方法上使用 @PreAuthorize、@Secured 等註解，支援細粒度的權限控制
public class SecurityConfig {

    @Autowired
    private EmployeeUserDetailsService userDetailsService; // 注入自定義的用戶詳情服務- 用於從資料庫載入用戶資訊和權限

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler; // 注入未授權處理器- 當用戶未登入或 Token 無效時的處理邏輯

    // 【已修正】@Bean 方法本身不應該有 @Autowired 註解。
    // Spring 會自動將這個 Bean 注入到需要它的地方。

    /**
     * 製造 JWT Token 檢查器（門口警衛）
     * 每個 API 請求都會先經過這個警衛檢查身分證（JWT token）
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() { // 創建 JWT Token 過濾器 Bean- 這個過濾器會在每個請求中檢查和驗證 JWT Token
        return new AuthTokenFilter();
    }

    /**
     * 製造認證提供者（帳號密碼檢查員）
     * 接收傳入的 username 和 password -> 透過 userDetailsService(我們開發者提供) 取得資料庫中儲存的加密密碼 ->
     * 透過
     * passwordEncoder(Spring提供) 比對密碼是否相符
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {// Spring Security 要使用我們設定的身分驗證提供者
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();// 創建基於資料庫的認證提供者
        authProvider.setUserDetailsService(userDetailsService); // 設定用戶詳情服務，用於載入用戶資訊
        authProvider.setPasswordEncoder(passwordEncoder()); // 設定密碼編碼器，用於驗證密碼
        return authProvider;
    }

    /**
     * 製造認證管理員（認證協調中心）
     * 負責協調各種認證方式
     */
    @Bean // 配置認證管理器 - Spring Security 的核心認證組件- 在登入時用於驗證用戶憑證
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 製造密碼加密器（密碼保險箱）
     * 負責把明文密碼加密，或檢查密碼是否正確
     */
    @Bean // 配置密碼編碼器
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 製造安全過濾鏈（整個安全檢查流程）
     * 這是最重要的方法，定義了所有安全規則
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // 禁用 CSRF (跨站請求偽造) 保護 ，因為使用 JWT 進行無狀態認證，不需要 CSRF 保護， REST API 通常不使用 CSRF 保護
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 啟用 CORS 配置- 允許前端跨域請求後端 API- 配置來源由 corsConfigurationSource() 方法定義
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // 設定異常處理 - 當用戶未認證時，由 unauthorizedHandler 處理，通常返回 401 Unauthorized 狀態碼
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置會話管理為無狀態 STATELESS：不創建和使用 HTTP 會話，完全依賴 JWT Token 進行認證，符合 REST API 無狀態原則
                .authorizeHttpRequests(auth -> auth
                        // 1. ====== 白名單 (一定要先列) ======
                        .requestMatchers("/api/auth/**").permitAll() // 允許所有認證相關 API 無需登入
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/order/addForm").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/employee-users/reset-password").permitAll()
                        // WebSocket 相關路徑
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/websocket/**").permitAll()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/departments", "/api/departments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/positions", "/api/positions/**").authenticated()
                        // 通訊錄相關 API - 允許所有已登入用戶訪問
                        .requestMatchers(HttpMethod.GET, "/api/employee-users").authenticated()

                        // 2. ====== 需要權限的 API ======
                        // 員工個別查詢和管理功能
                        .requestMatchers(HttpMethod.GET, "/api/employee-users/**")
                        .hasAnyAuthority("EMPLOYEE_VIEW", "EMPLOYEE_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/employee-users").hasAuthority("EMPLOYEE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/employee-users/**").hasAuthority("EMPLOYEE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/employee-users/**").hasAuthority("EMPLOYEE_MANAGE")
                        // 系統
                        .requestMatchers(HttpMethod.GET, "/api/system-logs", "/api/system-logs/**")
                        .hasAnyAuthority("SYSTEM_LOG_VIEW", "SYSTEM_LOG_MANAGE")
                        .requestMatchers("/api/system-logs/**").hasAuthority("SYSTEM_LOG_MANAGE")

                        // 物料管理權限
                        .requestMatchers(HttpMethod.GET, "/api/depot/materials", "/api/depot/materials/**")
                        .hasAuthority("INVENTORY_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/depot/materials").hasAuthority("INVENTORY_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/depot/materials/**").hasAuthority("INVENTORY_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/depot/materials/**").hasAuthority("INVENTORY_MANAGE")

                        // 庫存異動紀錄權限
                        .requestMatchers(HttpMethod.GET, "/api/depot/transactions")
                        .hasAuthority("INVENTORY_HISTORY_VIEW")

                        // 入庫單權限
                        .requestMatchers(HttpMethod.GET, "/api/depot/inbound-receipts",
                                "/api/depot/inbound-receipts/**")
                        .hasAnyAuthority("INBOUND_VIEW", "INBOUND_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/depot/inbound-receipts").hasAuthority("INBOUND_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/depot/inbound-receipts/**")
                        .hasAuthority("INBOUND_MANAGE")

                        // 供應商管理 API
                        .requestMatchers(HttpMethod.GET, "/api/supplier/list", "/api/supplier/{id}")
                        .hasAnyAuthority("SUPPLIER_VIEW", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/supplier/add").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/supplier/update").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/supplier/{id}").hasAuthority("SUPPLIER_MANAGE")

                        // 採購訂單 API
                        .requestMatchers(HttpMethod.GET, "/api/order/list", "/api/order/edit/**",
                                "/api/order/supplier-ratio", "/api/order/amount-per-month")
                        .hasAnyAuthority("SUPPLIER_VIEW", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/order/insert").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/order/update").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/order/delete/**").hasAuthority("SUPPLIER_MANAGE")

                        // 工單管理 API
                        .requestMatchers(HttpMethod.GET, "/api/workorder", "/api/workorder/**",
                                "/api/workorder/{woId}/materials")
                        .hasAnyAuthority("WORKORDER_VIEW", "WORKORDER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/workorder", "/api/workorder/picking")
                        .hasAuthority("WORKORDER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/workorder/**").hasAuthority("WORKORDER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/workorder/**").hasAuthority("WORKORDER_MANAGE")

                        // BOM 管理 API
                        .requestMatchers(HttpMethod.GET, "/api/boms/**").hasAnyAuthority("BOM_VIEW", "BOM_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/boms/add").hasAuthority("BOM_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/boms/update").hasAuthority("BOM_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/boms/**").hasAuthority("BOM_MANAGE")

                        // === 機台管理模組 ===
                        // 機台查詢 - 管理員、一般員工
                        .requestMatchers(HttpMethod.GET, "/api/machine/**")
                        .hasAnyAuthority("MACHINE_VIEW", "MACHINE_MANAGE")

                        // 機台新增 - 僅管理員可新增機台設備資料
                        .requestMatchers(HttpMethod.POST, "/api/machine").hasAuthority("MACHINE_MANAGE")

                        // 機台更新 - 僅管理員可修改機台設備資料和參數設定
                        .requestMatchers(HttpMethod.PUT, "/api/machine/**").hasAuthority("MACHINE_MANAGE")

                        // 機台刪除 - 僅管理員可刪除機台設備資料（需謹慎操作）
                        .requestMatchers(HttpMethod.DELETE, "/api/machine/**").hasAuthority("MACHINE_MANAGE")

                        // === 機台檔案管理模組 ===
                        // 檔案查詢下載 - 管理員和一般員工均可查看和下載機台相關技術文件
                        .requestMatchers(HttpMethod.GET, "/api/files/**")
                        .hasAnyAuthority("MACHINE_FILE_VIEW", "MACHINE_FILE_MANAGE")

                        // 檔案上傳 - 僅管理員可上傳機台技術文件、操作手冊等資料
                        .requestMatchers(HttpMethod.POST, "/api/files").hasAuthority("MACHINE_FILE_MANAGE")

                        // 檔案更新 - 僅管理員可更新機台檔案資訊和內容
                        .requestMatchers(HttpMethod.PUT, "/api/files/**").hasAuthority("MACHINE_FILE_MANAGE")

                        // 檔案刪除 - 僅管理員可刪除過期或錯誤的機台檔案
                        .requestMatchers(HttpMethod.DELETE, "/api/files/**").hasAuthority("MACHINE_FILE_MANAGE")

                        // === 機台維修管理模組 ===
                        // 維修記錄查詢 - 管理員可查看所有維修記錄，一般員工可查看自己提交的維修申請
                        .requestMatchers(HttpMethod.GET, "/api/repair/**")
                        .hasAnyAuthority("REPAIR_VIEW", "REPAIR_MANAGE")

                        // 維修申請表單 - 一般員工可填寫維修申請
                        .requestMatchers(HttpMethod.POST, "/api/repair").hasAuthority("REPAIR_CREATE")

                        // 管理員維修管理 - 管理員可更新維修狀態和查看管理介面
                        .requestMatchers(HttpMethod.PUT, "/api/repair/**").hasAuthority("REPAIR_MANAGE")

                        // === 機台保養管理模組 ===
                        // 保養記錄查詢 - 管理員可查看所有保養記錄，一般員工可查看相關保養計劃
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/**")
                        .hasAnyAuthority("MAINTENANCE_VIEW", "MAINTENANCE_MANAGE")

                        // 保養計劃建立 - 僅管理員可建立機台定期保養計劃和臨時保養任務
                        .requestMatchers(HttpMethod.POST, "/api/maintenance").hasAuthority("MAINTENANCE_MANAGE")

                        // 保養記錄更新 - 僅管理員可更新保養狀態和執行結果
                        .requestMatchers(HttpMethod.PUT, "/api/maintenance/**")
                        .hasAuthority("MAINTENANCE_MANAGE")

                        // 保養計劃刪除 - 僅管理員可刪除錯誤或過期的保養計劃
                        .requestMatchers(HttpMethod.DELETE, "/api/maintenance/**")
                        .hasAuthority("MAINTENANCE_MANAGE")

                        // 物料管理 API
                        .requestMatchers(HttpMethod.GET, "/api/material/**")
                        .hasAnyAuthority("MATERIAL_VIEW", "MATERIAL_MANAGE")

                        // 領料單管理 API
                        .requestMatchers(HttpMethod.GET, "/api/picking-orders/**")
                        .hasAnyAuthority("PICKING_ORDER_VIEW", "PICKING_ORDER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/picking-orders").hasAuthority("PICKING_ORDER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/picking-orders/**")
                        .hasAnyAuthority("PICKING_ORDER_MANAGE", "PICKING_ORDER_APPROVE")
                        .requestMatchers(HttpMethod.DELETE, "/api/picking-orders/**")
                        .hasAuthority("PICKING_ORDER_MANAGE")

                        // ====== 請假管理 API ======
                        .requestMatchers(HttpMethod.GET, "/api/leave/form-data").hasAuthority("LEAVE_APPLY_SELF")
                        .requestMatchers(HttpMethod.GET, "/api/leave/records", "/api/leave/records/**")
                        .hasAnyAuthority("LEAVE_VIEW_SELF", "LEAVE_VIEW_DEPARTMENT", "LEAVE_MANAGE_ALL")
                        .requestMatchers(HttpMethod.POST, "/api/leave/records").hasAuthority("LEAVE_APPLY_SELF")
                        .requestMatchers(HttpMethod.PUT, "/api/leave/records/{uuid}/status")
                        .hasAuthority("LEAVE_APPROVE")
                        .requestMatchers(HttpMethod.PUT, "/api/leave/records/**")
                        .hasAnyAuthority("LEAVE_MANAGE_ALL", "LEAVE_EDIT_SELF")
                        .requestMatchers(HttpMethod.DELETE, "/api/leave/records/**")
                        .hasAnyAuthority("LEAVE_MANAGE_ALL", "LEAVE_DELETE_SELF")

                        // ====== 請假附件管理 API ======
                        .requestMatchers(HttpMethod.GET, "/api/leave/attachments/**")
                        .hasAnyAuthority("LEAVE_VIEW_SELF", "LEAVE_VIEW_DEPARTMENT", "LEAVE_MANAGE_ALL")
                        .requestMatchers(HttpMethod.POST, "/api/leave/{uuid}/attachments")
                        .hasAnyAuthority("LEAVE_EDIT_SELF", "LEAVE_MANAGE_ALL")
                        .requestMatchers(HttpMethod.DELETE, "/api/leave/attachments/**")
                        .hasAnyAuthority("LEAVE_DELETE_SELF", "LEAVE_MANAGE_ALL")

                        // 3. 其他所有 /api/** 都要驗證 (暫時允許所有 /api/** 訪問，用於調試)
                        .requestMatchers("/api/**").permitAll() // 暫時註解掉這行，測試權限控制(暫時先減因為要測試)
                        // .requestMatchers("/api/**").denyAll()// (暫時先加測試用)

                        // 4. 非 API 路徑 (前端路徑) 全部放行其他所有請求都允許訪 - 主要是前端路由，不需要後端驗證
                        .anyRequest().permitAll());

        http.authenticationProvider(authenticationProvider()); // 註冊認證提供者 - 將自定義的認證提供者加入到 Spring Security
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        // 將 JWT Token 過濾器加入到過濾器鏈中，位置在 UsernamePasswordAuthenticationFilter
        // 之前，這樣每個請求都會先經過 JWT 驗證

        return http.build();
        // 建立並返回配置好的安全過濾器鏈
    }

    /**
     * 製造 CORS 設定來源（跨域存取規則）
     * 決定哪些網站可以呼叫我們的 API
     */
    @Bean // 配置允許的前端來源
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:5173", "http://172.22.34.82:5173")); // 配置允許的前端來源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));// 允許的 HTTP 方法
        configuration.setAllowedHeaders(Arrays.asList("*"));// 允許所有 HTTP 標頭
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // 允許傳送憑證 (如
                                                                                        // Cookies、Authorization
                                                                                        // headers)
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

/*
 * 1. 權限層級設計
 * 
 * 無需認證 (permitAll)
 * ↓
 * 需要認證 (authenticated)
 * ↓
 * 需要特定權限 (hasAuthority/hasAnyAuthority)
 * 
 * JWT Filter 整合機制
 * 
 * 1. 過濾器順序
 * 
 * 請求 → JWT Filter → Spring Security Filter Chain → Controller
 * 
 * 2. JWT Filter 作用
 * 
 * - 從請求標頭提取 Bearer Token
 * - 驗證 Token 有效性和過期時間
 * - 從 Token 中提取用戶資訊
 * - 設定 SecurityContext，供後續權限驗證使用
 * 
 * 3. 與權限系統整合
 * 
 * - JWT Token 包含用戶權限資訊
 * - Spring Security 根據 Token 中的權限進行 API 訪問控制
 * - 支援動態權限載入和細粒度控制
 */