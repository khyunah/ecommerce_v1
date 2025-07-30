package com.loopers.application.like;

import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeFacade {
    private final LikeService likeService;
    private final ProductService productService;

    public boolean create(LikeActionCommand command){
        if(productService.existsById(command.refProductId())){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다.");
        }
        return likeService.create(LikeActionCommand.toDomain(command));
    }

    public boolean delete(LikeActionCommand command) {
        if(productService.existsById(command.refProductId())){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다.");
        }
        return likeService.delete(LikeActionCommand.toDomain(command));
    }
}
