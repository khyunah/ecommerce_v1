package com.loopers.application.like.in;

import com.loopers.domain.like.Like;

public record LikeCreateCommand(
        Long refUserId,
        Long refProductId
) {
    public static Like toDomain(LikeCreateCommand command) {
        return Like.from(
                command.refUserId,
                command.refProductId
        );
    }
}
