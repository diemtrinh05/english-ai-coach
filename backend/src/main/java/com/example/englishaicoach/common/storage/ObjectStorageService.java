package com.example.englishaicoach.common.storage;

/** Ranh giới lưu media bên ngoài PostgreSQL, độc lập với SDK lưu trữ. */
public interface ObjectStorageService {

    /** Trả về tham chiếu media; adapter quyết định cách lưu và biểu diễn tham chiếu. */
    String store(byte[] content, String contentType);
}
