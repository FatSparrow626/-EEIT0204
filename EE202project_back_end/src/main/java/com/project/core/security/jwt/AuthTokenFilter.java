package com.project.core.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.project.core.security.EmployeeUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmployeeUserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 放行 /api/auth/** 和 /api/order/addForm
        String path = request.getServletPath();
        if (path.startsWith("/api/auth/") || path.equals("/api/order/addForm")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 1. AuthTokenFilter 自己從請求中解析出 JWT 字串
            String jwt = parseJwt(request);
            // 2. AuthTokenFilter 直接使用它自己的 jwtUtils 工具來驗證 JWT
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // 3. 驗證成功後，繼續使用 jwtUtils 來解析出使用者名稱
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // 4. 使用 userDetailsService 工具來載入使用者完整資料
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 將使用者資訊打包，放入 SecurityContextHolder，完成本次請求的身分認證
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        // 將請求交給過濾器鏈的下一個過濾器
        filterChain.doFilter(request, response);
    }

    /**
     * 解析 JWT 字串
     * 
     * @param request
     * @return String JWT 字串，若無則回傳 null
     */
    private String parseJwt(HttpServletRequest request) {
        // 1. 從請求中，根據 "Authorization" 這個 key，取得整個標頭的值
        // headerAuth 的值會是 "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWI..."
        String headerAuth = request.getHeader("Authorization");

        // 2. 檢查這個值是否存在，並且是否是以 "Bearer " 開頭
        // 這是為了確保我們拿到的是一個合規的 JWT 標頭
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // 3. 只擷取我們需要的 JWT 字串本體。
            // 因為"Bearer " 總共 7 個字元 (B, e, a, r, e, r, 空格)，所以從第 7 個 index 開始，若檢查通過，就從第
            // 7個字開始擷取字串。
            return headerAuth.substring(7);
        }

        // 4. 沒有標頭或格式不對 -> null
        return null;
    }
}
