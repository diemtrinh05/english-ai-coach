package com.example.englishaicoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Điểm khởi động backend và nơi quét tập trung các kiểu {@code @ConfigurationProperties}.
 * Cách quét này giữ việc bind cấu hình tách khỏi business logic và tránh đăng ký thủ công
 * từng nhóm thuộc tính.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EnglishAiCoachBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnglishAiCoachBackendApplication.class, args);
    }
}
