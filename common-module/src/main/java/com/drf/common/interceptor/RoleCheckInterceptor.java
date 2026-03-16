package com.drf.common.interceptor;

import com.drf.common.model.CommonResponse;
import com.drf.common.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCheckInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestURI = request.getRequestURI();
        String roleHeader = request.getHeader("X-User-Role");

        if (roleHeader == null || roleHeader.trim().isEmpty()) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다.");
            return false;
        }

        if (requestURI.startsWith("/admin/")) {
            if (!Role.ADMIN.name().equals(roleHeader)) {
                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
                return false;
            }
            return true;
        }

        if (!Role.USER.name().equals(roleHeader)) {
            writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
            return false;
        }

        return true;
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String code, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(CommonResponse.failure(code, message))
        );
    }
}
