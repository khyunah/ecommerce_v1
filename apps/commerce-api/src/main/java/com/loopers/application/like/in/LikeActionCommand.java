package com.loopers.application.like.in;

import com.loopers.domain.like.Like;

public record LikeActionCommand(
        Long refUserId,
        Long refProductId
) {
    public static Like toDomain(LikeActionCommand command) {
        return Like.from(
                command.refUserId,
                command.refProductId
        );
    }
}
