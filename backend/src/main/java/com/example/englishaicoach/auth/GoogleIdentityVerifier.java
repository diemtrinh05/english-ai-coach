package com.example.englishaicoach.auth;

public interface GoogleIdentityVerifier {
    VerifiedGoogleIdentity verify(String idToken);
}
