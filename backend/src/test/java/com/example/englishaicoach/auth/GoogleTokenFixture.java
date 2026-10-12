package com.example.englishaicoach.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;

final class GoogleTokenFixture {
    static final Instant NOW = Instant.parse("2026-10-12T00:00:00Z");
    static final String CLIENT = "fixture-server.apps.googleusercontent.com";
    static final RSAKey KEY = generate("fixture-google-key");
    private GoogleTokenFixture() { }
    static RSAKey generate(String kid) {
        try { return new RSAKeyGenerator(2048).keyID(kid).generate(); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    static JWTClaimsSet.Builder claims(String sub, String email, String name) {
        return new JWTClaimsSet.Builder().issuer("https://accounts.google.com")
                .audience(CLIENT).subject(sub).claim("email",email).claim("email_verified",true)
                .claim("name",name).expirationTime(Date.from(NOW.plusSeconds(300)));
    }
    static String token(String sub, String email, String name) {
        return sign(claims(sub,email,name).build(), KEY, KEY.getKeyID());
    }
    static String sign(JWTClaimsSet claims, RSAKey key, String kid) {
        try {
            var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(kid).build(),claims);
            token.sign(new RSASSASigner(key));
            return token.serialize();
        } catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    static String jwks(RSAKey key) {
        return new com.nimbusds.jose.jwk.JWKSet(key.toPublicJWK()).toString();
    }
}
