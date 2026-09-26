package com.example.englishaicoach.core.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class EncryptedTokenStore implements TokenStore {
    private static final String KEY_ALIAS = "english_ai_coach_auth_v1";
    private static final String PREFS = "auth_credentials";
    private static final String ENTRY = "encrypted_session";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public EncryptedTokenStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Nullable @Override public synchronized TokenSession read() {
        String encoded = preferences.getString(ENTRY, null);
        if (encoded == null) return null;
        try {
            byte[] payload = Base64.decode(encoded, Base64.NO_WRAP);
            int ivLength = payload[0] & 0xff;
            if (ivLength != 12 || payload.length <= 1 + ivLength) throw new IllegalStateException("Invalid credential envelope");
            byte[] iv = new byte[ivLength];
            System.arraycopy(payload, 1, iv, 0, ivLength);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] plaintext = cipher.doFinal(payload, 1 + ivLength, payload.length - 1 - ivLength);
            TokenSession session = gson.fromJson(new String(plaintext, StandardCharsets.UTF_8), TokenSession.class);
            if (session == null) throw new IllegalStateException("Missing credential session");
            return new TokenSession(session.sessionId(), session.accessToken(), session.refreshToken());
        } catch (Exception failure) {
            // Bỏ thông tin xác thực hỏng hoặc khóa không còn dùng được; không ghi dữ liệu nhạy cảm vào log.
            preferences.edit().remove(ENTRY).commit();
            return null;
        }
    }

    @Override public synchronized void write(@Nullable TokenSession session) {
        if (session == null) {
            if (!preferences.edit().remove(ENTRY).commit()) throw new IllegalStateException("Credential clear failed");
            return;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key());
            byte[] iv = cipher.getIV();
            byte[] plaintext = gson.toJson(session).getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] payload = new byte[1 + iv.length + ciphertext.length];
            payload[0] = (byte) iv.length;
            System.arraycopy(iv, 0, payload, 1, iv.length);
            System.arraycopy(ciphertext, 0, payload, 1 + iv.length, ciphertext.length);
            if (!preferences.edit().putString(ENTRY, Base64.encodeToString(payload, Base64.NO_WRAP)).commit()) {
                throw new IllegalStateException("Credential write failed");
            }
        } catch (Exception failure) {
            throw new IllegalStateException("Credential encryption failed", failure);
        }
    }

    private SecretKey key() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey existing = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        if (existing != null) return existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build());
        return generator.generateKey();
    }
}
