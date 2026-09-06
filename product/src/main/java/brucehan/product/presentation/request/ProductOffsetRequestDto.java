package brucehan.product.presentation.request;

public record ProductOffsetRequestDto(
        int pageNumber,
        int size
) {
}
