package com.drf.member.common.interceptor;

import com.drf.member.common.auth.Role;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RoleCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestURI = request.getRequestURI();
        String roleHeader = request.getHeader("X-User-Role");

        // role 헤더 검증
        if (roleHeader == null || roleHeader.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 관리자 권한 검증
        if (requestURI.startsWith("/admin/")) {
            if (!Role.ADMIN.name().equals(roleHeader)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            return true;
        }

        // 유저 권한 검증
        if (!Role.USER.name().equals(roleHeader)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}