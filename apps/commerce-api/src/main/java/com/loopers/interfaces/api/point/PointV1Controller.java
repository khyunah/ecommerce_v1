package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;
import com.loopers.application.point.PointFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final PointFacade pointFacade;

    @GetMapping("/{userId}")
    public ResponseEntity<PointV1Dto.PointInfoResponse> get(@PathVariable String userId, @RequestHeader Map<String, String> headers){
        if(headers.get("X-USER-ID").isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        PointInfo pointInfo = pointFacade.get(userId);
        if(pointInfo == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        PointV1Dto.PointInfoResponse response = PointV1Dto.PointInfoResponse.from(pointInfo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
