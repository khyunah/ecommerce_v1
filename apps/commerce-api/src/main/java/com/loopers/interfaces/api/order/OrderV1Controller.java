package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderSummaryResult;
import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.support.auth.AuthenticatedUserIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller {

    private final OrderFacade orderFacade;

    @PostMapping
    public ResponseEntity<OrderV1Dto.OrderCreateResponse> create(
            HttpServletRequest headers,
            @RequestBody OrderV1Dto.OrderCreateRequest request){
        Long userId = AuthenticatedUserIdProvider.getUserId(headers);
        OrderCreateCommand command = OrderV1Dto.OrderCreateRequest.toOrderCreateCommand(userId, request);
        OrderSummaryResult result = orderFacade.placeOrder(command);
        OrderV1Dto.OrderCreateResponse response = OrderV1Dto.OrderCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
