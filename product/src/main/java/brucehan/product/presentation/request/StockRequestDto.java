package brucehan.product.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequestDto(
        @NotNull
        Long productId,

        @Min(value = 1, message = "최소 1개 이상 주문해야 합니다")
        Integer quantity,
        Long orderId
) {
}
