package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public enum ProductSortType {
    LATEST,     // 최신순
    LIKE_COUNT; // 좋아요 수순

    public static ProductSortType fromString(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return LATEST; // 기본값
        }
        
        try {
            return ProductSortType.valueOf(sortType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                "유효하지 않은 정렬 타입입니다. (LATEST, LIKE_COUNT 중 하나를 입력해주세요)");
        }
    }
}
