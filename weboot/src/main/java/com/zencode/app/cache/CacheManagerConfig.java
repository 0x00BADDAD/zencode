package com.zencode.app.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;



@Configuration
@EnableCaching
public class CacheManagerConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeine1 = Caffeine.newBuilder()
            .expireAfterWrite(1800, TimeUnit.SECONDS)
            .maximumSize(500);

        Caffeine<Object, Object> caffeine2 = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.DAYS)
            .maximumSize(500);

        CaffeineCache accessTokenCache = new CaffeineCache("accessTokenCache", caffeine1.build());
        CaffeineCache refreshTokenCache = new CaffeineCache("refreshTokenCache", caffeine2.build());
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(accessTokenCache, refreshTokenCache));
        return manager;
    }




    @Bean
    @Primary
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

       // RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
       //         .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
       //                 new GenericJackson2JsonRedisSerializer()
       //         ))
       //        .disableCachingNullValues();


        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache "sessions" with 5 days TTL
        cacheConfigurations.put("sessions",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .entryTtl(Duration.ofDays(5)));

        // Cache "refresh Token" with 5 days TTL
        cacheConfigurations.put("refTok",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .entryTtl(Duration.ofDays(5)));

        // Cache "access Token" with 40 minutes of TTL
        cacheConfigurations.put("accTok",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .entryTtl(Duration.ofMinutes(40)));


        // Default TTL for all others: 30 minutes
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

