package brucehan.order.persentation;

import brucehan.order.application.OrderCoordinator;
import brucehan.order.application.OrderService;
import brucehan.order.application.RedisLockService;
import brucehan.order.application.dto.CreateOrderResult;
import brucehan.order.persentation.request.CreateOrderRequest;
import brucehan.order.persentation.request.PlaceOrderRequest;
import brucehan.order.persentation.response.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final RedisLockService redisLockService;
    private final OrderCoordinator orderCoordinator;

    @PostMapping("/v1/order")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderResult result = orderService.createOrder(request.toCommand());
        return new CreateOrderResponse(result.orderId());
    }

    @PostMapping("/order/place")
    public void placeOrder(@RequestBody PlaceOrderRequest request) {
        String lockKey = "order:" + request.orderId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.orderId().toString());
        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }
        try {
            orderCoordinator.placeOrder(request.toCommand());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }
}
