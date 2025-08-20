package com.loopers.infrastructure.payment;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("PG API 호출 실패 - Method: {}, Status: {}, Reason: {}", 
                  methodKey, response.status(), response.reason());
        
        switch (response.status()) {
            case 400:
                return new PgBadRequestException("PG 요청 형식이 올바르지 않습니다.");
            case 500:
                return new PgServerException("PG 서버 오류가 발생했습니다.");
            case 503:
                return new PgServiceUnavailableException("PG 서비스를 사용할 수 없습니다.");
            default:
                return new PgException("PG 호출 중 알 수 없는 오류가 발생했습니다. Status: " + response.status());
        }
    }
    
    public static class PgException extends RuntimeException {
        public PgException(String message) {
            super(message);
        }
    }
    
    public static class PgBadRequestException extends PgException {
        public PgBadRequestException(String message) {
            super(message);
        }
    }
    
    public static class PgServerException extends PgException {
        public PgServerException(String message) {
            super(message);
        }
    }
    
    public static class PgServiceUnavailableException extends PgException {
        public PgServiceUnavailableException(String message) {
            super(message);
        }
    }
}
