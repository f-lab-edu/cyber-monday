package brucehan.order.presentation.request;

import brucehan.order.application.dto.CreateOrderCommand;

import java.util.List;

public record CreateOrderRequest(
        List<OrderItem> items
) {
    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
                items.stream()
                        .map(item -> new CreateOrderCommand.OrderItem(item.productId, item.quantity, item.productName, item.productPrice))
                        .toList()
        );
    }

    public record OrderItem(
            Long productId,
            Long quantity,
            String productName,
            Long productPrice
    ) {

    }
}
