package brucehan.product.presentation.response;

public record StockResponseDto(
        Integer quantity,
        Long productId
) {
}
