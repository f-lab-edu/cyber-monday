package brucehan.order.application.dto;

import brucehan.order.domain.OrderItem;

import java.util.List;

public record CreateOrderCommand(
        List<OrderItem> items
) {
    public record OrderItem(
            Long productId,
            Long quantity
    ) {

    }
}
