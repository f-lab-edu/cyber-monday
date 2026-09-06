package brucehan.product.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequestDto(
        @NotNull
        Long productId,

        @Min(value = 1L, message = "최소 1개 이상 주문해야 합니다")
        Long quantity
) {
}
