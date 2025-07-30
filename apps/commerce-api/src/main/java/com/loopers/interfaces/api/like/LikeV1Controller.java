package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.in.LikeCreateCommand;
import com.loopers.support.auth.AuthenticatedUserIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like")
public class LikeV1Controller {
    private final LikeFacade likeFacade;

    @PostMapping("/products")
    public ResponseEntity<LikeV1Dto.LikeCreateResponse> create(@RequestHeader HttpServletRequest headers,
                                                              @RequestBody LikeV1Dto.LikeCreateRequest request)
    {
        Long userId = AuthenticatedUserIdProvider.getUserId(headers);
        LikeCreateCommand command = LikeV1Dto.LikeCreateRequest.toCommand(request, userId);
        boolean isLiked = likeFacade.create(command);
        LikeV1Dto.LikeCreateResponse response = LikeV1Dto.LikeCreateResponse.from(command, isLiked);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
