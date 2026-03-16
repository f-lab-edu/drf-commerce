package com.drf.common.resolver;

import com.drf.common.exception.UnauthorizedAccessException;
import com.drf.common.model.AuthInfo;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthInfoResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthInfo.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String idHeader = webRequest.getHeader("X-User-Id");

        if (idHeader == null || idHeader.trim().isEmpty()) {
            throw new UnauthorizedAccessException();
        }

        return new AuthInfo(Long.parseLong(idHeader));
    }
}
