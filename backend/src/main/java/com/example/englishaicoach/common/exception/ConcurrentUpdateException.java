package com.example.englishaicoach.common.exception;

import org.springframework.http.HttpStatus;

public final class ConcurrentUpdateException extends ApiException {

    private static final long serialVersionUID = 1L;

    public ConcurrentUpdateException() {
        super(
                HttpStatus.CONFLICT,
                ApiErrorCodes.CONCURRENT_UPDATE,
                "Dữ liệu đã thay đổi trên máy chủ. Vui lòng tải lại và thử lại.");
    }
}
