package brucehan.product.presentation.request;

import brucehan.product.application.dto.ProductBuyCommandDto;

import java.util.List;

public record ProductBuyRequestDto(
        String requestId,
        List<ProductInfo> productInfos
) {
    public ProductBuyCommandDto toCommand() {
        return new ProductBuyCommandDto(
                requestId,
                productInfos
                        .stream()
                        .map(info -> new ProductBuyCommandDto.ProductInfo(info.productId, info.quantity))
                        .toList()
        );
    }

    public record ProductInfo(
            Long productId,
            Long quantity
    ) {

    }
}
