package com.example.englishaicoach.common.storage;

/** Adapter disabled mặc định cho môi trường chưa cấu hình Object Storage. */
public final class UnavailableObjectStorageService implements ObjectStorageService {

    @Override
    public String store(byte[] content, String contentType) {
        throw new IllegalStateException("Object Storage chưa được cấu hình.");
    }
}
