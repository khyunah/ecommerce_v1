package com.loopers.application.like;


import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.domain.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeFacade {
    private final LikeService likeService;

    public boolean create(LikeActionCommand command){
        return likeService.create(LikeActionCommand.toDomain(command));
    }

    public boolean delete(LikeActionCommand command) {
        return likeService.delete(LikeActionCommand.toDomain(command));
    }
}
