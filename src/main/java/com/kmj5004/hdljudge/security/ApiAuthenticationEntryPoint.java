package com.kmj5004.hdljudge.security;

import tools.jackson.databind.ObjectMapper;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.common.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        ErrorCode code = resolveCode(request);
        ErrorResponse body = new ErrorResponse(
            code.getCode(),
            code.getDefaultMessage(),
            request.getRequestURI(),
            OffsetDateTime.now(),
            null
        );

        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(body));
    }

    private ErrorCode resolveCode(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.ATTR_AUTH_ERROR);
        if (attr instanceof ApiException ex) {
            return ex.getErrorCode();
        }
        return ErrorCode.UNAUTHORIZED;
    }
}
