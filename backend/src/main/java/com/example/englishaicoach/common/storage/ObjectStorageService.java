package com.example.englishaicoach.common.storage;

/** Ranh giới lưu media bên ngoài PostgreSQL, độc lập với SDK lưu trữ. */
public interface ObjectStorageService {

    /** Với audio từ vựng, trả URL HTTPS ổn định và công khai để client tải media. */
    String store(byte[] content, String contentType);
}
