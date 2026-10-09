package com.example.englishaicoach.common.security;

import com.example.englishaicoach.common.exception.ApiErrorResponse;
import com.example.englishaicoach.common.logging.RequestCorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

public final class SecurityErrorWriter {
    private final Clock clock;
    private final ObjectMapper mapper;

    public SecurityErrorWriter(Clock clock, ObjectMapper mapper) {
        this.clock = clock;
        this.mapper = mapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
            int status, String code, String message) throws IOException {
        request.setAttribute(RequestCorrelationFilter.ERROR_CODE_ATTRIBUTE, code);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), new ApiErrorResponse(clock.instant(), status,
                code, message, request.getRequestURI(), List.of()));
    }
}
