package com.kmj5004.hdljudge.security;

import tools.jackson.databind.ObjectMapper;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.common.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        ErrorCode code = ErrorCode.FORBIDDEN;
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
}
