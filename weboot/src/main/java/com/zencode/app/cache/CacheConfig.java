package com.zencode.app.cache;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> myCache() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();
    }

}

