package brucehan.product.presentation.request;

public record ProductOffsetRequestDto(
        int offset,
        int limit,
        int size
) {
}
