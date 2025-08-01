package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.application.like.out.LikedProductsResult;
import com.loopers.support.auth.AuthenticatedUserIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like")
public class LikeV1Controller {
    private final LikeFacade likeFacade;

    @PostMapping("/products")
    public ResponseEntity<LikeV1Dto.LikeActionResponse> create(
            HttpServletRequest headers,
            @RequestBody LikeV1Dto.LikeActionRequest request){
        Long userId = AuthenticatedUserIdProvider.getUserId(headers);
        LikeActionCommand command = LikeV1Dto.LikeActionRequest.toCommand(request, userId);
        boolean isLiked = likeFacade.create(command);
        LikeV1Dto.LikeActionResponse response = LikeV1Dto.LikeActionResponse.from(command, isLiked);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/products")
    public ResponseEntity<LikeV1Dto.LikeActionResponse> delete(
            HttpServletRequest headers,
            @RequestBody LikeV1Dto.LikeActionRequest request){
        Long userId = AuthenticatedUserIdProvider.getUserId(headers);
        LikeActionCommand command = LikeV1Dto.LikeActionRequest.toCommand(request, userId);
        boolean isLiked = likeFacade.delete(command);
        LikeV1Dto.LikeActionResponse response = LikeV1Dto.LikeActionResponse.from(command, isLiked);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<LikeV1Dto.LikedProductResponse>> getLikedProducts(
            HttpServletRequest headers,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long refUserId = AuthenticatedUserIdProvider.getUserId(headers);

        Page<LikedProductsResult> result = likeFacade.getLikedProducts(refUserId, pageable);
        Page<LikeV1Dto.LikedProductResponse> response = result.map(LikeV1Dto.LikedProductResponse::from);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
