package brucehan.order.infrastructure.product.dto;

import java.util.List;

public record ProductBuyApiRequest(
        String requestId,
        List<ProductInfo> productInfos
) {
    public record ProductInfo(
            Long productId,
            Long quantity
    ) {

    }
}
