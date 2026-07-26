package brucehan.product.presentation.request;

public record StockRequestDto(
        Long productId,
        Integer quantity,
        Long orderId
) {
}
