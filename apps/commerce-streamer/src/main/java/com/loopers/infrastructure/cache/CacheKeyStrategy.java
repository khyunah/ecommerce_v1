package com.loopers.infrastructure.cache;

/**
 * 캐시 키 전략 정의
 * 일관된 캐시 키 패턴을 유지하기 위한 전략 클래스
 */
public final class CacheKeyStrategy {

    // 캐시 키 접두사
    public static final String PRODUCT_DETAIL_PREFIX = "product:detail:";
    public static final String PRODUCT_LIST_PREFIX = "product:list:";
    public static final String POPULAR_PRODUCTS_PREFIX = "product:popular:";
    public static final String PRODUCT_SEARCH_PREFIX = "product:search:";
    public static final String CATEGORY_PREFIX = "category:";
    
    // TTL 설정 (초 단위)
    public static final long PRODUCT_DETAIL_TTL = 3600L; // 1시간
    public static final long PRODUCT_LIST_TTL = 1800L;   // 30분
    public static final long POPULAR_PRODUCTS_TTL = 900L; // 15분
    public static final long SEARCH_RESULTS_TTL = 600L;   // 10분

    private CacheKeyStrategy() {
        // 유틸리티 클래스
    }

    /**
     * 상품 상세 캐시 키 생성
     */
    public static String productDetail(Long productId) {
        return PRODUCT_DETAIL_PREFIX + productId;
    }

    /**
     * 상품 목록 캐시 키 생성
     */
    public static String productList(String category, String sortType, int page, int size) {
        return PRODUCT_LIST_PREFIX + category + ":" + sortType + ":" + page + ":" + size;
    }

    /**
     * 인기 상품 캐시 키 생성
     */
    public static String popularProducts(String period, int limit) {
        return POPULAR_PRODUCTS_PREFIX + period + ":" + limit;
    }

    /**
     * 검색 결과 캐시 키 생성
     */
    public static String searchResults(String keyword, String category, String sortType, int page, int size) {
        String sanitizedKeyword = keyword.replaceAll("[^a-zA-Z0-9가-힣]", "_");
        return PRODUCT_SEARCH_PREFIX + sanitizedKeyword + ":" + category + ":" + sortType + ":" + page + ":" + size;
    }

    /**
     * 카테고리 캐시 키 생성
     */
    public static String category(String categoryCode) {
        return CATEGORY_PREFIX + categoryCode;
    }

    /**
     * 패턴 기반 키 생성 (삭제용)
     */
    public static String productDetailPattern() {
        return PRODUCT_DETAIL_PREFIX + "*";
    }

    public static String productListPattern() {
        return PRODUCT_LIST_PREFIX + "*";
    }

    public static String popularProductsPattern() {
        return POPULAR_PRODUCTS_PREFIX + "*";
    }

    public static String searchPattern() {
        return PRODUCT_SEARCH_PREFIX + "*";
    }

    /**
     * 특정 상품과 관련된 목록 캐시 패턴
     * (실제 구현에서는 더 정교한 매칭이 필요할 수 있음)
     */
    public static String[] getProductRelatedPatterns(Long productId) {
        return new String[]{
            productDetailPattern(),
            productListPattern(),
            popularProductsPattern(),
            searchPattern()
        };
    }
}
