package com.loopers.infrastructure.payment;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PgFeignConfig {

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                3000L, TimeUnit.MILLISECONDS,    // connectTimeout: 3초
                10000L, TimeUnit.MILLISECONDS,   // readTimeout: 10초
                true                             // followRedirects
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new PgFeignErrorDecoder();
    }
}
