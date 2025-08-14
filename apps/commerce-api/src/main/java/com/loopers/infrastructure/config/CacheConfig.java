package com.loopers.infrastructure.config;

import com.loopers.config.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;
import java.util.Arrays;

@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisProperties.class)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 기본 Caffeine 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)  // 최대 10,000개 엔트리
                .expireAfterWrite(Duration.ofMinutes(30))  // 기본 TTL 30분
                .recordStats());  // 통계 수집

        // 캐시별 개별 설정도 가능
        cacheManager.setCacheNames(Arrays.asList("productDetail", "productList"));

        return cacheManager;
    }
}
