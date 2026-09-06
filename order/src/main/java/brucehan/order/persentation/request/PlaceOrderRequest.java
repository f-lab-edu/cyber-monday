package brucehan.order.persentation.request;

import brucehan.order.application.dto.PlaceOrderCommand;

public record PlaceOrderRequest(
        Long orderId
) {
    public PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(orderId);
    }
}
