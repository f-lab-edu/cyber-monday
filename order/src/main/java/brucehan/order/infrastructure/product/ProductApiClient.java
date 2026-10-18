package brucehan.order.infrastructure.product;

import brucehan.order.infrastructure.product.dto.ProductBuyApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyApiResponse;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
public class ProductApiClient {
    private final RestClient restClient;
    private final ProductErrorTranslator productErrorTranslator;

    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public ProductBuyApiResponse buy(ProductBuyApiRequest request) {
        try {
            return restClient
                    .post()
                    .uri("/products/buy")
                    .body(request)
                    .retrieve()
                    .body(ProductBuyApiResponse.class);
        } catch (HttpClientErrorException e) {
            throw productErrorTranslator.translate(e);
        }
    }



    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public ProductBuyCancelApiResponse cancel(ProductBuyCancelApiRequest request) {
        try {
            return restClient
                    .post()
                    .uri("/products/buy/cancel")
                    .body(request)
                    .retrieve()
                    .body(ProductBuyCancelApiResponse.class);
        } catch (HttpClientErrorException e) {
            throw productErrorTranslator.translate(e);
        }
    }
}
