package com.example.englishaicoach.config;

import com.example.englishaicoach.common.clock.BusinessTimeProvider;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình nguồn thời gian dùng chung của backend.
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    BusinessTimeProvider businessTimeProvider(Clock applicationClock) {
        return new BusinessTimeProvider(applicationClock);
    }
}
