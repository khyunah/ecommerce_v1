package com.loopers.interfaces.api.like;

import com.loopers.application.like.in.LikeCreateCommand;

public class LikeV1Dto {
    public record LikeCreateRequest(
            Long refProductId
    ){
        public static LikeCreateCommand toCommand(LikeCreateRequest request, Long refUserId) {
            return new LikeCreateCommand(
                    request.refProductId,
                    refUserId
            );
        }
    }

    public record LikeCreateResponse(
            Long refProductId,
            boolean isLiked
    ) {
        public static LikeCreateResponse from(LikeCreateCommand command, boolean isLiked) {
            return new LikeCreateResponse(
                    command.refProductId(),
                    isLiked
            );
        }
    }
}
