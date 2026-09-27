package com.example.englishaicoach.common.logging;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";
    public static final String ERROR_CODE_ATTRIBUTE = RequestCorrelationFilter.class.getName() + ".errorCode";

    private static final Logger LOG = LoggerFactory.getLogger(RequestCorrelationFilter.class);
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        String requestId = isUuid(incoming)
                ? incoming
                : UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();
        String previousRequestId = MDC.get(REQUEST_ID);
        String previousTraceId = MDC.get(TRACE_ID);

        request.setAttribute(REQUEST_ID, requestId);
        request.setAttribute(TRACE_ID, traceId);
        response.setHeader(HEADER, requestId);
        MDC.put(REQUEST_ID, requestId);
        MDC.put(TRACE_ID, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            var event = LOG.atInfo()
                    .addKeyValue("module", "http")
                    .addKeyValue("operation", request.getMethod())
                    .addKeyValue("durationMs", durationMs)
                    .addKeyValue("result", response.getStatus());
            Object errorCode = request.getAttribute(ERROR_CODE_ATTRIBUTE);
            if (errorCode instanceof String code) {
                event.addKeyValue("errorCode", code);
            }
            event.log("request_complete");
            restore(REQUEST_ID, previousRequestId);
            restore(TRACE_ID, previousTraceId);
        }
    }

    private static void restore(String key, String previous) {
        if (previous == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previous);
        }
    }

    private static boolean isUuid(String value) {
        if (value == null || value.length() != 36) {
            return false;
        }
        try {
            return UUID.fromString(value).toString().equalsIgnoreCase(value);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
