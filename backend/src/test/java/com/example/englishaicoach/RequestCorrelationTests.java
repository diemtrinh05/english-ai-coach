package com.example.englishaicoach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.englishaicoach.audit.AuditCorrelation;
import com.example.englishaicoach.common.clock.BusinessTimeProvider;
import com.example.englishaicoach.common.exception.ConcurrentUpdateException;
import com.example.englishaicoach.common.exception.GlobalExceptionHandler;
import com.example.englishaicoach.common.exception.IdempotencyKeyReuseException;
import com.example.englishaicoach.common.logging.RequestCorrelationFilter;

class RequestCorrelationTests {

    private final CorrelationController controller = new CorrelationController();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new RequestCorrelationFilter())
                .setControllerAdvice(new GlobalExceptionHandler(new BusinessTimeProvider(
                        Clock.fixed(Instant.parse("2026-09-27T00:00:00Z"), ZoneOffset.UTC))))
                .build();
    }

    @Test
    void retainsSafeClientIdAndConnectsAuditDetails() throws Exception {
        String clientId = UUID.randomUUID().toString();
        mockMvc.perform(get("/test/correlation")
                        .header(RequestCorrelationFilter.HEADER, clientId))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestCorrelationFilter.HEADER, clientId));

        assertThat(controller.requestId).isEqualTo(clientId);
        assertThat(controller.traceId).matches("[0-9a-f-]{36}").isNotEqualTo(clientId);
        assertThat(controller.auditDetails).containsEntry("traceId", controller.traceId);
        assertThat(controller.auditDetails).containsEntry("action", "UPDATE");
        assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID)).isNull();
        assertThat(MDC.get(RequestCorrelationFilter.TRACE_ID)).isNull();
    }

    @Test
    void generatesDistinctServerTracesForReusedClientId() throws Exception {
        String clientId = UUID.randomUUID().toString();
        mockMvc.perform(get("/test/correlation").header(RequestCorrelationFilter.HEADER, clientId))
                .andExpect(status().isOk());
        String firstTraceId = controller.traceId;

        mockMvc.perform(get("/test/correlation").header(RequestCorrelationFilter.HEADER, clientId))
                .andExpect(status().isOk());
        assertThat(controller.requestId).isEqualTo(clientId);
        assertThat(controller.traceId).isNotEqualTo(firstTraceId);
        assertThat(controller.auditDetails).containsEntry("traceId", controller.traceId);
    }

    @Test
    void createsSafeIdWhenHeaderIsMissingOrUnsafe() throws Exception {
        mockMvc.perform(get("/test/correlation"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestCorrelationFilter.HEADER));
        assertThat(controller.requestId).matches("[0-9a-f-]{36}");

        mockMvc.perform(get("/test/correlation")
                        .header(RequestCorrelationFilter.HEADER, "token\r\nAuthorization: Bearer secret"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestCorrelationFilter.HEADER));
        assertThat(controller.requestId).matches("[0-9a-f-]{36}");
        assertThat(controller.requestId).doesNotContain("secret");
        assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID)).isNull();
    }

    @Test
    void auditHelperDoesNotAddTraceOutsideRequest() {
        assertThat(AuditCorrelation.withTraceId(Map.of("action", "APPROVE")))
                .containsExactlyInAnyOrderEntriesOf(Map.of("action", "APPROVE"));
        assertThat(AuditCorrelation.withTraceId(Map.of("traceId", "untrusted"))).isEmpty();
        assertThat(AuditCorrelation.withTraceId(null)).isEmpty();
    }

    @Test
    void requestLogNeverContainsCredentialsOrPayload() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestCorrelationFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            mockMvc.perform(post("/test/correlation")
                            .header("Authorization", "Bearer access-secret")
                            .header("X-Fcm-Token", "fcm-secret")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("refresh-secret"))
                    .andExpect(status().isMethodNotAllowed());

            assertThat(appender.list).hasSize(1);
            ILoggingEvent event = appender.list.getFirst();
            assertThat(event.getFormattedMessage()).isEqualTo("request_complete");
            assertThat(event.getMDCPropertyMap()).containsKeys("requestId", "traceId");
            assertThat(event.getMDCPropertyMap().toString())
                    .doesNotContain("access-secret", "fcm-secret", "refresh-secret");
            assertThat(event.getKeyValuePairs().toString())
                    .doesNotContain("access-secret", "fcm-secret", "refresh-secret");
            assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID)).isNull();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void distinguishesCanonicalConflictCodesInStructuredRequestLog() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestCorrelationFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            mockMvc.perform(get("/test/concurrent-update"))
                    .andExpect(status().isConflict());
            mockMvc.perform(get("/test/idempotency-reuse"))
                    .andExpect(status().isConflict());

            assertThat(appender.list).hasSize(2);
            assertThat(errorCode(appender.list.get(0))).isEqualTo("CONCURRENT_UPDATE");
            assertThat(errorCode(appender.list.get(1))).isEqualTo("IDEMPOTENCY_KEY_REUSE");
            assertThat(appender.list).allSatisfy(event -> {
                assertThat(event.getMDCPropertyMap()).containsKeys("requestId", "traceId");
                assertThat(event.getKeyValuePairs().toString()).contains("result=\"409\"");
            });
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private static String errorCode(ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
                .filter(pair -> pair.key.equals("errorCode"))
                .map(pair -> pair.value.toString())
                .findFirst()
                .orElse(null);
    }

    @RestController
    private static final class CorrelationController {

        private String requestId;
        private String traceId;
        private Map<String, Object> auditDetails;

        @GetMapping("/test/correlation")
        String correlation() {
            requestId = MDC.get(RequestCorrelationFilter.REQUEST_ID);
            traceId = MDC.get(RequestCorrelationFilter.TRACE_ID);
            auditDetails = AuditCorrelation.withTraceId(Map.of("action", "UPDATE"));
            return "ok";
        }

        @GetMapping("/test/concurrent-update")
        void concurrentUpdate() {
            throw new ConcurrentUpdateException();
        }

        @GetMapping("/test/idempotency-reuse")
        void idempotencyReuse() {
            throw new IdempotencyKeyReuseException();
        }
    }
}
