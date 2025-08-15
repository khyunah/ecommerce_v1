package com.loopers.application.like;

import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.application.like.out.LikedProductsResult;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.dto.LikedProductDto;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeFacade {
    private final LikeService likeService;
    private final ProductService productService;

    @Transactional
    public boolean create(LikeActionCommand command){
        if(!productService.existsById(command.refProductId())){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다.");
        }
        boolean result =  likeService.create(LikeActionCommand.toDomain(command));
        if(result){
            Product product = productService.getDetail(command.refProductId());
            productService.increaseLikeCount(product);
        }
        return result;
    }

    @Transactional
    public boolean delete(LikeActionCommand command) {
        if(!productService.existsById(command.refProductId())){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다.");
        }
        boolean result =  likeService.delete(LikeActionCommand.toDomain(command));
        if(result){
            Product product = productService.getDetail(command.refProductId());
            productService.decreaseLikeCount(product);
        }
        return result;
    }

    public Page<LikedProductsResult> getLikedProducts(Long userId, Pageable pageable) {
        Page<LikedProductDto> likedProducts = likeService.getLikedProducts(userId, pageable);
        return likedProducts.map(LikedProductsResult::from);
    }
}
