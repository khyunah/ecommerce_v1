package com.loopers.interfaces.api.like;

import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.application.like.out.LikedProductsResult;

import java.math.BigDecimal;

public class LikeV1Dto {
    public record LikeActionRequest(
            Long refProductId
    ){
        public static LikeActionCommand toCommand(LikeActionRequest request, Long refUserId) {
            return new LikeActionCommand(
                    request.refProductId,
                    refUserId
            );
        }
    }

    public record LikeActionResponse(
            Long refProductId,
            boolean isLiked
    ) {
        public static LikeActionResponse from(LikeActionCommand command, boolean isLiked) {
            return new LikeActionResponse(
                    command.refProductId(),
                    isLiked
            );
        }
    }

    public record LikedProductResponse(
            Long productId,
            String productName,
            BigDecimal originalPrice,
            BigDecimal sellingPrice,
            String saleStatus,
            Long likeId,
            Boolean liked
    ) {
        public static LikedProductResponse from(LikedProductsResult result){
            return new LikedProductResponse(
                    result.productId(),
                    result.productName(),
                    result.originalPrice(),
                    result.sellingPrice(),
                    result.saleStatus(),
                    result.likeId(),
                    result.liked()
            );
        }
    }
}
