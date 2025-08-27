package com.loopers.application.like;

import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.application.like.out.LikedProductsResult;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.dto.LikedProductDto;
import com.loopers.domain.like.event.LikeEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeFacade {
    private final LikeService likeService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean create(LikeActionCommand command){
        // 1. 상품 존재 여부 검증
        if(!productService.existsById(command.refProductId())){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다.");
        }
        
        // 2. 좋아요 생성
        boolean result = likeService.create(LikeActionCommand.toDomain(command));
        
        // 3. 좋아요 성공 시 이벤트 발행
        if(result){
            log.info("좋아요 생성 성공 - 집계 이벤트 발행 userId: {}, productId: {}", 
                    command.refUserId(), command.refProductId());
            eventPublisher.publishEvent(
                    LikeEvent.createLikeAdded(command.refUserId(), command.refProductId())
            );
        }
        
        return result;
    }

    @Transactional
    public boolean delete(LikeActionCommand command) {
        // 1. 상품 존재 여부 검증
        if(!productService.existsById(command.refProductId())){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다.");
        }
        
        // 2. 좋아요 삭제
        boolean result = likeService.delete(LikeActionCommand.toDomain(command));
        
        // 3. 좋아요 삭제 성공 시 이벤트 발행
        if(result){
            log.info("좋아요 삭제 성공 - 집계 이벤트 발행 userId: {}, productId: {}", 
                    command.refUserId(), command.refProductId());
            eventPublisher.publishEvent(
                    LikeEvent.createLikeRemoved(command.refUserId(), command.refProductId())
            );
        }
        
        return result;
    }

    public Page<LikedProductsResult> getLikedProducts(Long userId, Pageable pageable) {
        Page<LikedProductDto> likedProducts = likeService.getLikedProducts(userId, pageable);
        return likedProducts.map(LikedProductsResult::from);
    }
}
