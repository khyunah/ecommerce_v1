package com.loopers.domain.user.event;

/**
 * 유저 행동 타입 정의
 */
public enum UserActionType {
    // 상품 관련
    PRODUCT_VIEW,           // 상품 조회
    PRODUCT_CLICK,          // 상품 클릭
    PRODUCT_LIKE,           // 상품 좋아요
    PRODUCT_UNLIKE,         // 상품 좋아요 취소
    PRODUCT_CART_ADD,       // 장바구니 추가
    PRODUCT_CART_REMOVE,    // 장바구니 제거
    PRODUCT_WISHLIST_ADD,   // 위시리스트 추가
    PRODUCT_WISHLIST_REMOVE, // 위시리스트 제거
    
    // 주문 관련
    ORDER_START,            // 주문 시작
    ORDER_COMPLETE,         // 주문 완료
    ORDER_CANCEL,           // 주문 취소
    ORDER_PAYMENT_COMPLETE, // 결제 완료
    ORDER_PAYMENT_FAIL,     // 결제 실패
    
    // 검색 관련
    SEARCH,                 // 검색
    SEARCH_NO_RESULT,       // 검색 결과 없음
    
    // 사용자 관련
    USER_LOGIN,             // 로그인
    USER_LOGOUT,            // 로그아웃
    USER_REGISTER,          // 회원가입
    USER_PROFILE_VIEW,      // 프로필 조회
    USER_PROFILE_UPDATE,    // 프로필 수정
    
    // 페이지 관련
    PAGE_VIEW,              // 페이지 조회
    PAGE_EXIT,              // 페이지 이탈
    
    // 기타
    API_CALL,               // API 호출
    ERROR                   // 오류 발생
}
