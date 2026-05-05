package com.app.config;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT拦截器 - 验证token、管理员权限校验
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    // 管理员专属路径前缀（写操作）
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/api/user/list",
            "/api/user/save",
            "/api/user/delete",
            "/api/user/updateStatus",
            "/api/productionPlan/save",
            "/api/productionPlan/delete",
            "/api/material/save",
            "/api/material/delete",
            "/api/material/inbound",
            "/api/material/outbound",
            "/api/quality/save",
            "/api/quality/delete",
            "/api/stats/",
            "/api/materialRequest/approve",
            "/api/qualityFeedback/handle"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "未登录");
            return false;
        }

        // 验证token
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);

            // 将userId和role设置为请求属性，供Controller使用
            Integer role = claims.get("role", Integer.class);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            // 检查管理员权限
            String uri = request.getRequestURI();
            if (isAdminPath(uri)) {
                if (role == null || role != Constants.ROLE_ADMIN) {
                    sendError(response, 403, "无权限");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            sendError(response, 401, "未登录");
            return false;
        }
    }

    /**
     * 判断是否为管理员专属路径
     */
    private boolean isAdminPath(String uri) {
        for (String path : ADMIN_PATHS) {
            if (uri.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
