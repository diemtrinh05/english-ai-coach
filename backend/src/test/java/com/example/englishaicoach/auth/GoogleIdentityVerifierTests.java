package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.config.GoogleIdentityProperties;
import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GoogleIdentityVerifierTests {
    private final Clock clock = Clock.fixed(GoogleTokenFixture.NOW,ZoneOffset.UTC);
    private GoogleJwksSource source() {
        return new GoogleJwksSource(clock,() -> new GoogleJwksSource.Document(
                GoogleTokenFixture.jwks(GoogleTokenFixture.KEY),"public, max-age=3600"));
    }
    private JwksGoogleIdentityVerifier verifier(GoogleJwksSource source, String client) {
        return new JwksGoogleIdentityVerifier(new GoogleIdentityProperties(client),source,clock,
                Validation.buildDefaultValidatorFactory().getValidator());
    }
    private void invalid(JwksGoogleIdentityVerifier verifier,String token) {
        assertThatThrownBy(() -> verifier.verify(token)).isInstanceOfSatisfying(ApiException.class,error -> {
            assertThat(error.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(error.getCode()).isEqualTo("UNAUTHORIZED");
            assertThat(error.toString()).doesNotContain(token);
        });
    }
    @Test void acceptsBothGoogleIssuersAndOnlyVerifiedClaimsWithoutTrustingMutableName() {
        var verifier=verifier(source(),GoogleTokenFixture.CLIENT);
        for(String issuer:List.of("https://accounts.google.com","accounts.google.com")) {
            String token=GoogleTokenFixture.sign(GoogleTokenFixture.claims("sub","learner@example.test",null)
                    .issuer(issuer).build(),GoogleTokenFixture.KEY,GoogleTokenFixture.KEY.getKeyID());
            var identity=verifier.verify(token);
            assertThat(identity.subject()).isEqualTo("sub");
            assertThat(identity.email()).isEqualTo("learner@example.test");
            assertThat(identity.name()).isNull();
            assertThat(identity.toString()).doesNotContain("learner@example.test","sub");
        }
    }
    @Test void rejectsIssuerAudienceExpirySubjectEmailVerificationAndEmailShape() {
        var verifier=verifier(source(),GoogleTokenFixture.CLIENT);
        var original=GoogleTokenFixture.claims("sub","learner@example.test","Tên").build();
        List<JWTClaimsSet> invalidClaims=List.of(
                new JWTClaimsSet.Builder(original).issuer("https://attacker.test").build(),
                new JWTClaimsSet.Builder(original).audience("other-app").build(),
                new JWTClaimsSet.Builder(original).expirationTime(Date.from(GoogleTokenFixture.NOW)).build(),
                new JWTClaimsSet.Builder(original).expirationTime(null).build(),
                new JWTClaimsSet.Builder(original).subject(" ").build(),
                new JWTClaimsSet.Builder(original).subject("s".repeat(256)).build(),
                new JWTClaimsSet.Builder(original).claim("email_verified",false).build(),
                new JWTClaimsSet.Builder(original).claim("email_verified","true").build(),
                new JWTClaimsSet.Builder(original).claim("email",null).build(),
                new JWTClaimsSet.Builder(original).claim("email","invalid").build(),
                new JWTClaimsSet.Builder(original).claim("email","x".repeat(256)+"@example.test").build());
        for(var claims:invalidClaims) invalid(verifier,GoogleTokenFixture.sign(claims,GoogleTokenFixture.KEY,GoogleTokenFixture.KEY.getKeyID()));
    }
    @Test void rejectsMalformedAlgorithmUnknownKidAndWrongSignature() {
        var verifier=verifier(source(),GoogleTokenFixture.CLIENT);
        invalid(verifier,"not-a-token"); invalid(verifier,"x".repeat(16385));
        invalid(verifier,GoogleTokenFixture.sign(GoogleTokenFixture.claims("sub","a@example.test","A").build(),
                GoogleTokenFixture.generate("foreign"),GoogleTokenFixture.KEY.getKeyID()));
        invalid(verifier,GoogleTokenFixture.sign(GoogleTokenFixture.claims("sub","a@example.test","A").build(),
                GoogleTokenFixture.KEY,"unknown"));
        String unsigned=new com.nimbusds.jwt.PlainJWT(GoogleTokenFixture.claims("sub","a@example.test","A").build()).serialize();
        invalid(verifier,unsigned);
    }
    @Test void missingConfigurationAndJwksOutageFailClosedWithoutExposingCredentials() {
        var calls=new AtomicInteger();
        var source=new GoogleJwksSource(clock,()-> { calls.incrementAndGet(); throw new java.io.IOException("sensitive-provider-payload"); });
        String token=GoogleTokenFixture.token("sub","a@example.test","A");
        for(var verifier:List.of(verifier(source,""),verifier(source,GoogleTokenFixture.CLIENT))) {
            assertThatThrownBy(()->verifier.verify(token)).isInstanceOfSatisfying(ApiException.class,error -> {
                assertThat(error.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(error.getCode()).isEqualTo("INTERNAL_ERROR");
                assertThat(error.toString()).doesNotContain(token,"sensitive-provider-payload");
            });
        }
        assertThat(calls).hasValue(1);
    }
    @Test void cachesGoogleKeysAndThrottlesUnknownKidRefresh() {
        var calls=new AtomicInteger();
        var source=new GoogleJwksSource(clock,()-> {
            calls.incrementAndGet(); return new GoogleJwksSource.Document(GoogleTokenFixture.jwks(GoogleTokenFixture.KEY),"max-age=3600");
        });
        var verifier=verifier(source,GoogleTokenFixture.CLIENT);
        for(int i=0;i<3;i++) verifier.verify(GoogleTokenFixture.token("sub","a@example.test","A"));
        for(int i=0;i<3;i++) invalid(verifier,GoogleTokenFixture.sign(GoogleTokenFixture.claims("sub","a@example.test","A").build(),GoogleTokenFixture.KEY,UUID.randomUUID().toString()));
        assertThat(calls).hasValue(1);
        assertThat(GoogleJwksSource.GOOGLE_JWKS.toString()).isEqualTo("https://www.googleapis.com/oauth2/v3/certs");
    }
    @Test void rejectsMalformedEmptyAndOversizedJwksAsUnavailable() {
        for(String body:List.of("not-json","{\"keys\":[]}","x".repeat(131073))) {
            var source=new GoogleJwksSource(clock,()->new GoogleJwksSource.Document(body,""));
            assertThatThrownBy(()->source.key("key")).isInstanceOfSatisfying(ApiException.class,error ->
                    assertThat(error.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        }
    }
}
