package brucehan.order.application.dto;

import java.util.List;

public record CreateOrderCommand(
        List<OrderItem> items
) {
    public record OrderItem(
            Long productId,
            Long quantity,
            String productName,
            Long productPrice
    ) {

    }
}
