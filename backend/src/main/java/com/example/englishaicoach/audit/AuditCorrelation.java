package com.example.englishaicoach.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.MDC;

import com.example.englishaicoach.common.logging.RequestCorrelationFilter;

public final class AuditCorrelation {

    private AuditCorrelation() {
    }

    public static Map<String, Object> withTraceId(Map<String, Object> details) {
        Map<String, Object> correlated = new LinkedHashMap<>(details == null ? Map.of() : details);
        correlated.remove("traceId");
        String traceId = MDC.get(RequestCorrelationFilter.TRACE_ID);
        if (traceId != null) {
            correlated.put("traceId", traceId);
        }
        return correlated;
    }
}
