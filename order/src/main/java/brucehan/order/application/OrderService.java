package brucehan.order.application;

import brucehan.order.application.dto.CreateOrderCommand;
import brucehan.order.application.dto.CreateOrderResult;
import brucehan.order.application.dto.OrderItemDto;
import brucehan.order.domain.Order;
import brucehan.order.domain.OrderItem;
import brucehan.order.infrastructure.OrderItemRepository;
import brucehan.order.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .map(item -> new OrderItem(order.getId(), item.productId(), item.quantity()))
                .toList();
        orderItemRepository.saveAll(orderItems);

        return new CreateOrderResult(order.getId());
    }

    @Transactional(readOnly = true)
    public OrderItemDto getOrder(String orderNumber) {
        Long orderId = orderRepository.findByOrderNumber(orderNumber)
                .map(Order::getId)
                .orElseThrow(RuntimeException::new);
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);
        return new OrderItemDto(
                orderItems.stream()
                        .map(item -> new OrderItemDto.OrderItem(item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }

    @Transactional
    public void request(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.request();
        orderRepository.save(order);
    }

    @Transactional
    public void complete(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.complete();
        orderRepository.save(order);
    }

    @Transactional
    public void failOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.failOrder();
        orderRepository.save(order);
    }

    @Transactional
    public void approve(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.approve(order.getPaymentKey());
        orderRepository.save(order);
    }

    @Transactional
    public void markUnknown(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.markUnknown();
        orderRepository.save(order);
    }
}
