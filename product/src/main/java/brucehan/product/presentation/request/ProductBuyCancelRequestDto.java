package brucehan.product.presentation.request;

import brucehan.product.application.dto.ProductBuyCancelCommandDto;

public record ProductBuyCancelRequestDto(
        String requestId
) {
    public ProductBuyCancelCommandDto toCommand() {
        return new ProductBuyCancelCommandDto(requestId);
    }
}
