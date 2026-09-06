package brucehan.order.infrastructure.product;

import brucehan.order.infrastructure.product.dto.ProductBuyApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyApiResponse;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class ProductApiClient {
    private final RestClient restClient;

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public ProductBuyApiResponse buy(ProductBuyApiRequest request) {
        return restClient
                .post()
                .uri("/products/buy")
                .body(request)
                .retrieve()
                .body(ProductBuyApiResponse.class);
    }

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public ProductBuyCancelApiResponse cancel(ProductBuyCancelApiRequest request) {
        return restClient
                .post()
                .uri("/v1/products/buy/cancel")
                .body(request)
                .retrieve()
                .body(ProductBuyCancelApiResponse.class);
    }
}
