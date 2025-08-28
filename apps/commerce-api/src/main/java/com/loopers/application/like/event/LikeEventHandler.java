package com.loopers.application.like.event;

import com.loopers.domain.like.event.LikeEvent;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventHandler {

    private final ProductService productService;

    @EventListener
    @Async
    @Transactional
    public void handleLikeEvent(LikeEvent event) {
        try {
            log.info("좋아요 이벤트 처리 시작 - userId: {}, productId: {}, action: {}, thread: {}", 
                    event.getUserId(), event.getProductId(), event.getAction(), Thread.currentThread().getName());
            
            // 상품 존재 여부 재확인
            if (!productService.existsById(event.getProductId())) {
                log.warn("상품이 존재하지 않아 집계를 건너뜁니다. productId: {}", event.getProductId());
                return;
            }

            Product product = productService.getDetail(event.getProductId());
            
            switch (event.getAction()) {
                case ADDED -> {
                    productService.increaseLikeCount(product);
                    log.info("좋아요 수 증가 완료 - productId: {}, 현재 좋아요 수: {}", 
                            event.getProductId(), product.getLikeCount());
                }
                case REMOVED -> {
                    productService.decreaseLikeCount(product);
                    log.info("좋아요 수 감소 완료 - productId: {}, 현재 좋아요 수: {}", 
                            event.getProductId(), product.getLikeCount());
                }
            }
            
        } catch (Exception e) {
            log.error("좋아요 집계 업데이트 실패 - userId: {}, productId: {}, action: {}", 
                    event.getUserId(), event.getProductId(), event.getAction(), e);
        }
    }
}
