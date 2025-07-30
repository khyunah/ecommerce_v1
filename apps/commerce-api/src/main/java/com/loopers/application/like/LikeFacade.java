package com.loopers.application.like;


import com.loopers.application.like.in.LikeCreateCommand;
import com.loopers.domain.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeFacade {
    private final LikeService likeService;

    public boolean create(LikeCreateCommand command){
        return likeService.create(LikeCreateCommand.toDomain(command));
    }
}
