package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1Controller {

    private final UserFacade userFacade;

    @PostMapping
    public ResponseEntity<UserV1Dto.UserInfoResponse> register(@RequestBody UserV1Dto.UserRegisterRequest request) {
        if(null == request.gender() || request.gender().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        UserInfo userInfo = userFacade.register(request);
        UserV1Dto.UserInfoResponse response = UserV1Dto.UserInfoResponse.from(userInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/{loingId}")
    public ResponseEntity<UserV1Dto.UserInfoResponse> get(@PathVariable String loingId){
        UserInfo userInfo = userFacade.get(loingId);
        UserV1Dto.UserInfoResponse response = UserV1Dto.UserInfoResponse.from(userInfo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
