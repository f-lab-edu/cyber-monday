package brucehan.order.config;

import brucehan.order.infrastructure.product.ProductErrorTranslator;
import brucehan.order.infrastructure.product.ProductApiClient;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class ApiClientConfig {
    private final ProductErrorTranslator productErrorTranslator;

    @Bean
    public ProductApiClient productApiClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableAutomaticRetries()
                .build();

        return new ProductApiClient(
                RestClient.builder()
                        .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                        .baseUrl("http://localhost:8082")
                        .build(),
                productErrorTranslator
        );
    }
}
