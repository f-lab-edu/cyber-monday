package brucehan.order.application;

import brucehan.order.application.dto.OrderDto;
import brucehan.order.application.dto.PlaceOrderCommand;
import brucehan.order.domain.CompensationRegistry;
import brucehan.order.infrastructure.CompensationRegistryRepository;
import brucehan.order.infrastructure.product.ProductApiClient;
import brucehan.order.infrastructure.product.dto.ProductBuyApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyApiResponse;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiResponse;
import io.lettuce.core.AbstractRedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCoordinator {
    private final OrderService orderService;
    private final ProductApiClient productApiClient;
    private final CompensationRegistryRepository compensationRegistryRepository;

    public void placeOrder(PlaceOrderCommand command) {
        orderService.request(command.orderId());
        OrderDto orderDto = orderService.getOrder(command.orderId());

        try {
            // 결제 먼저

            ProductBuyApiRequest productBuyApiRequest = new ProductBuyApiRequest(
                    command.orderId().toString(),
                    orderDto.orderItems().stream()
                            .map(item -> new ProductBuyApiRequest.ProductInfo(item.productId(), item.quantity()))
                            .toList()
            );

            ProductBuyApiResponse buyApiResponse = productApiClient.buy(productBuyApiRequest);
            log.info("TODO buyApiResponse로 적립금 구현에 활용하기 {}", buyApiResponse.totalPrice());
            orderService.complete(command.orderId());
        } catch (Exception e) {
            log.error("롤백 : {}", command.orderId(), e);
            rollback(command.orderId());
        }
    }

    private void rollback(Long orderId) {
        try {
            ProductBuyCancelApiRequest productBuyCancelApiRequest = new ProductBuyCancelApiRequest(orderId.toString());
            ProductBuyCancelApiResponse productBuyCancelApiResponse = productApiClient.cancel(productBuyCancelApiRequest);
            if (productBuyCancelApiResponse.totalPrice() > 0) {
                log.info("적립금 환불 TODO");
            }
            orderService.fail(orderId);
        } catch (Exception e) {
            compensationRegistryRepository.save(new CompensationRegistry(orderId));
            throw e;
        }
    }
}
