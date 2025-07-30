package com.loopers.interfaces.api.like;

import com.loopers.application.like.in.LikeActionCommand;

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
}
