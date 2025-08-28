package com.loopers.application.user;

import com.loopers.domain.user.event.UserActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자 행동 추적 어노테이션
 * 메서드에 이 어노테이션을 붙이면 자동으로 사용자 행동을 추적
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackUserAction {
    
    /**
     * 행동 타입
     */
    UserActionType actionType();
    
    /**
     * 대상 타입 (예: PRODUCT, ORDER, USER)
     */
    String targetType() default "";
    
    /**
     * 대상 ID를 추출할 파라미터 이름
     */
    String targetIdParam() default "";
    
    /**
     * 사용자 ID를 추출할 파라미터 이름
     */
    String userIdParam() default "userId";
    
    /**
     * 추가 속성을 추출할 파라미터들
     */
    String[] propertyParams() default {};
}
