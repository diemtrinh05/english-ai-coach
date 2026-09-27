package com.example.englishaicoach.common.security;

import java.time.Duration;

/** Điểm thay thế lưu trạng thái khi cần triển khai phân tán. */
public interface RateLimitGate {
    boolean allow(String route, String remoteAddress, int maximum, Duration window);
}
