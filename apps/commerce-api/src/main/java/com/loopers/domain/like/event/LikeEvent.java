package com.loopers.domain.like.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LikeEvent {
    private final Long userId;
    private final Long productId;
    private final LikeAction action;
    private final LocalDateTime occurredAt;

    public LikeEvent(Long userId, Long productId, LikeAction action) {
        this.userId = userId;
        this.productId = productId;
        this.action = action;
        this.occurredAt = LocalDateTime.now();
    }

    public static LikeEvent createLikeAdded(Long userId, Long productId) {
        return new LikeEvent(userId, productId, LikeAction.ADDED);
    }

    public static LikeEvent createLikeRemoved(Long userId, Long productId) {
        return new LikeEvent(userId, productId, LikeAction.REMOVED);
    }

    public enum LikeAction {
        ADDED, REMOVED
    }
}
