package com.example.englishaicoach.auth;

import com.example.englishaicoach.config.GoogleIdentityProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import org.springframework.stereotype.Component;

@Component
public final class JwksGoogleIdentityVerifier implements GoogleIdentityVerifier {
    private final GoogleIdentityProperties properties;
    private final GoogleJwksSource keys;
    private final Clock clock;
    private final Validator validator;

    public JwksGoogleIdentityVerifier(GoogleIdentityProperties properties, GoogleJwksSource keys,
            Clock clock, Validator validator) {
        this.properties = properties;
        this.keys = keys;
        this.clock = clock;
        this.validator = validator;
    }

    @Override
    public VerifiedGoogleIdentity verify(String idToken) {
        if (properties.clientId() == null || properties.clientId().isBlank()) {
            throw GoogleAuthErrors.unavailable();
        }
        SignedJWT jwt;
        try {
            if (idToken == null || idToken.length() > 16384) {
                throw GoogleAuthErrors.invalid();
            }
            jwt = SignedJWT.parse(idToken);
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())
                    || jwt.getHeader().getKeyID() == null || jwt.getHeader().getKeyID().isBlank()) {
                throw GoogleAuthErrors.invalid();
            }
        } catch (java.text.ParseException exception) {
            throw GoogleAuthErrors.invalid();
        }
        // Lỗi JWKS giữ nguyên 503, không bị đánh đồng với credentials sai.
        var key = keys.key(jwt.getHeader().getKeyID());
        try {
            if (!jwt.verify(new RSASSAVerifier(key))) {
                throw GoogleAuthErrors.invalid();
            }
            var claims = jwt.getJWTClaimsSet();
            String issuer = claims.getIssuer();
            String subject = claims.getSubject();
            String email = claims.getStringClaim("email");
            if (!("accounts.google.com".equals(issuer) || "https://accounts.google.com".equals(issuer))
                    || claims.getAudience() == null || !claims.getAudience().contains(properties.clientId())
                    || claims.getExpirationTime() == null
                    || !claims.getExpirationTime().toInstant().isAfter(clock.instant())
                    || subject == null || subject.isBlank() || subject.length() > 255
                    || !Boolean.TRUE.equals(claims.getBooleanClaim("email_verified"))
                    || !validator.validate(new EmailClaim(email)).isEmpty()) {
                throw GoogleAuthErrors.invalid();
            }
            // name chỉ bắt buộc khi tạo account; login giữ metadata đã lưu ở DB.
            Object name = claims.getClaim("name");
            return new VerifiedGoogleIdentity(subject, email, name instanceof String text ? text : null);
        } catch (java.text.ParseException | com.nimbusds.jose.JOSEException exception) {
            throw GoogleAuthErrors.invalid();
        }
    }

    private record EmailClaim(@NotBlank @Email @Size(max = 255) String email) { }
}
