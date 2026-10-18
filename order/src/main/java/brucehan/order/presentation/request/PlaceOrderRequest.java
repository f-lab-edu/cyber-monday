package brucehan.order.presentation.request;

import brucehan.order.application.dto.PlaceOrderCommand;

public record PlaceOrderRequest(
        String orderNumber
) {
    public PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(orderNumber);
    }
}
