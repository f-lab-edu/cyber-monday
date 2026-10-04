package brucehan.order.application;

import brucehan.order.application.dto.OrderItemDto;
import brucehan.order.application.dto.PlaceOrderCommand;
import brucehan.order.config.exception.BusinessException;
import brucehan.order.domain.CompensationRegistry;
import brucehan.order.infrastructure.CompensationRegistryRepository;
import brucehan.order.infrastructure.product.ProductApiClient;
import brucehan.order.infrastructure.product.dto.ProductBuyApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyApiResponse;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiResponse;
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
        orderService.request(command.orderNumber());
        OrderItemDto orderItemDto = orderService.getOrder(command.orderNumber());

        try {
            // 결제 먼저
            orderService.approve(command.orderNumber());
            // 결제 완료
            ProductBuyApiRequest productBuyApiRequest = new ProductBuyApiRequest(
                    command.orderNumber(),
                    orderItemDto.orderItems().stream()
                            .map(item -> new ProductBuyApiRequest.ProductInfo(item.productId(), item.quantity()))
                            .toList()
            );

            ProductBuyApiResponse buyApiResponse = productApiClient.buy(productBuyApiRequest);
            log.info("TODO buyApiResponse로 적립금 구현에 활용하기 {}", buyApiResponse.totalPrice());
            orderService.complete(command.orderNumber());
        } catch (Exception e) {
            // TODO 결제가 실패했을 때 예외 로직 분리. 현재는 주문 실패만 있음.
            if (e instanceof BusinessException be) {
                log.warn("롤백 : {} - code : {}", command.orderNumber(), be.getErrorCode());
            } else {
                log.error("롤백 : {}", command.orderNumber(), e);
            }
            rollback(command.orderNumber());
            throw e;
        }
    }

    private void rollback(String orderNumber) {
        try {
            // TODO 결제 취소 추가
            ProductBuyCancelApiRequest productBuyCancelApiRequest = new ProductBuyCancelApiRequest(orderNumber);
            ProductBuyCancelApiResponse productBuyCancelApiResponse = productApiClient.cancel(productBuyCancelApiRequest);
            if (productBuyCancelApiResponse.totalPrice() > 0) {
                log.info("적립금 환불 TODO");
            }
            orderService.failOrder(orderNumber);
        } catch (Exception e) {
            log.error("보상 실패 : {}", orderNumber, e);
            try {
                compensationRegistryRepository.save(new CompensationRegistry(orderNumber, e.getClass().getSimpleName()));
            } catch (Exception saveEx) {
                log.error("보상 기록 실패 : {}", orderNumber, saveEx);
            }
        }
    }
}
