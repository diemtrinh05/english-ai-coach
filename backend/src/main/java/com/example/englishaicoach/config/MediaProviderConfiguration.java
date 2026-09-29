package com.example.englishaicoach.config;

import com.example.englishaicoach.common.storage.ObjectStorageService;
import com.example.englishaicoach.common.storage.UnavailableObjectStorageService;
import com.example.englishaicoach.vocabulary.TtsProvider;
import com.example.englishaicoach.vocabulary.UnavailableTtsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cấp adapter disabled đến khi một adapter thật được cấu hình tường minh. */
@Configuration
public class MediaProviderConfiguration {

    @Bean
    @ConditionalOnMissingBean(TtsProvider.class)
    TtsProvider unavailableTtsProvider() {
        return new UnavailableTtsProvider();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectStorageService.class)
    ObjectStorageService unavailableObjectStorageService() {
        return new UnavailableObjectStorageService();
    }
}
