package com.example.englishaicoach.auth;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.InputStream;
import java.net.URI;
import javax.net.ssl.HttpsURLConnection;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class GoogleJwksSource {
    static final URI GOOGLE_JWKS = URI.create("https://www.googleapis.com/oauth2/v3/certs");
    private static final int MAX_BYTES = 131072;
    private static final Pattern MAX_AGE = Pattern.compile("(?:^|,)\\s*max-age=(\\d+)");
    private final Clock clock;
    private final Fetcher fetcher;
    private List<JWK> keys = List.of();
    private Instant expiresAt = Instant.MIN;
    private Instant nextUnknownRefresh = Instant.MIN;

    @Autowired
    public GoogleJwksSource(Clock clock) {
        this(clock, productionFetcher());
    }

    GoogleJwksSource(Clock clock, Fetcher fetcher) {
        this.clock = clock;
        this.fetcher = fetcher;
    }

    synchronized RSAKey key(String kid) {
        Instant now = clock.instant();
        if (!now.isBefore(expiresAt)) {
            refresh(now);
        }
        RSAKey key = find(kid);
        if (key == null && !now.isBefore(nextUnknownRefresh)) {
            // Giới hạn refetch khi kid do client gửi không có trong cache.
            refresh(now);
            key = find(kid);
        }
        if (key == null) {
            throw GoogleAuthErrors.invalid();
        }
        return key;
    }

    private RSAKey find(String kid) {
        return keys.stream().filter(key -> kid.equals(key.getKeyID()))
                .filter(RSAKey.class::isInstance).map(RSAKey.class::cast)
                .filter(key -> key.getKeyUse() == null || "sig".equals(key.getKeyUse().identifier()))
                .filter(key -> key.getAlgorithm() == null || "RS256".equals(key.getAlgorithm().getName()))
                .filter(key -> !key.isPrivate()).findFirst().orElse(null);
    }

    private void refresh(Instant now) {
        try {
            Document document = fetcher.fetch();
            if (document.body().length() > MAX_BYTES) {
                throw new IllegalStateException();
            }
            List<JWK> received = JWKSet.parse(document.body()).getKeys();
            if (received.isEmpty()) {
                throw new IllegalStateException();
            }
            long seconds = 300;
            var matcher = MAX_AGE.matcher(document.cacheControl());
            if (matcher.find()) {
                seconds = Math.min(86400, Long.parseLong(matcher.group(1)));
            }
            keys = List.copyOf(received);
            expiresAt = now.plusSeconds(seconds);
            nextUnknownRefresh = now.plusSeconds(30);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw GoogleAuthErrors.unavailable();
        } catch (Exception exception) {
            // Không đưa response, token hoặc exception provider vào lỗi/log.
            throw GoogleAuthErrors.unavailable();
        }
    }

    private static Fetcher productionFetcher() {
        return () -> {
            // URL cố định; không đọc jku/x5u/URL từ token hoặc request.
            var connection = (HttpsURLConnection) GOOGLE_JWKS.toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);
            try {
                if (connection.getResponseCode() != 200) {
                    throw new IllegalStateException();
                }
                try (InputStream body = connection.getInputStream()) {
                    byte[] bytes = body.readNBytes(MAX_BYTES + 1);
                    if (bytes.length > MAX_BYTES) {
                        throw new IllegalStateException();
                    }
                    return new Document(new String(bytes, java.nio.charset.StandardCharsets.UTF_8),
                            java.util.Objects.toString(connection.getHeaderField("Cache-Control"), ""));
                }
            } finally {
                connection.disconnect();
            }
        };
    }

    interface Fetcher { Document fetch() throws Exception; }
    record Document(String body, String cacheControl) { }
}
