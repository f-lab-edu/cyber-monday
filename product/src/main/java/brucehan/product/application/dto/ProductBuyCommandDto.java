package brucehan.product.application.dto;

import java.util.List;

public record ProductBuyCommandDto(
        String requestId,
        List<ProductInfo> productInfos
) {
    public record ProductInfo(
            Long productId,
            Long quantity
    ) {
    }
}
