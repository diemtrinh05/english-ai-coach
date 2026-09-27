package com.example.englishaicoach.config;

import java.net.URI;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Danh sách origin được phép gọi API từ trình duyệt. */
@ConfigurationProperties("app.security.cors")
public record CorsPolicyProperties(List<String> allowedOrigins) {
    public CorsPolicyProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        for (String origin : allowedOrigins) {
            URI uri;
            try {
                uri = URI.create(origin);
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("CORS origin phải là origin HTTP(S) tường minh", exception);
            }
            if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
                throw new IllegalArgumentException("CORS origin phải là origin HTTP(S) tường minh");
            }
        }
    }
}
