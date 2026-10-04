package brucehan.order.application;

import brucehan.order.application.dto.CreateOrderCommand;
import brucehan.order.application.dto.CreateOrderResult;
import brucehan.order.application.dto.OrderItemDto;
import brucehan.order.config.constant.ErrorCode;
import brucehan.order.config.exception.BusinessException;
import brucehan.order.domain.Order;
import brucehan.order.domain.OrderItem;
import brucehan.order.infrastructure.OrderItemRepository;
import brucehan.order.infrastructure.OrderRepository;
import brucehan.order.infrastructure.product.ProductApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static brucehan.order.config.constant.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    // TODO createOrder 지우고 placeOrder 살리면 되는지 검토
    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        Order order = orderRepository.save(new Order());

        List<OrderItem> orderItems = command.items()
                .stream()
                .map(item -> {
                    return new OrderItem(order.getId(), item.productId(), item.quantity());
                })
                .toList();
        orderItemRepository.saveAll(orderItems);

        return new CreateOrderResult(order.getOrderNumber());
    }

    @Transactional(readOnly = true)
    public OrderItemDto getOrder(String orderNumber) {
        Long orderId = orderRepository.findByOrderNumber(orderNumber)
                .map(Order::getId)
                .orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);
        return new OrderItemDto(
                orderItems.stream()
                        .map(item -> new OrderItemDto.OrderItem(item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }

    @Transactional
    public void request(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        order.request();
    }

    @Transactional
    public void complete(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        order.complete();
    }

    @Transactional
    public void failOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        order.failOrder();
    }

    @Transactional
    public void approve(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        order.approve(order.getPaymentKey()); // PG 붙일 때 approve(pgResult.paymentKey())가 되어야 함
    }
}
