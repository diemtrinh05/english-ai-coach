package com.example.englishaicoach.auth;

public record VerifiedGoogleIdentity(String subject, String email, String name) {
    @Override
    public String toString() {
        return "VerifiedGoogleIdentity[claims=REDACTED]";
    }
}
