package brucehan.product.init;

import brucehan.product.domain.entity.Product;
import brucehan.product.domain.entity.Stock;
import brucehan.product.infrastructure.ProductJpaRepository;
import brucehan.product.infrastructure.StockJpaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test") // 테스트에서는 각 테스트가 직접 데이터를 준비한다. (stock.product_id 유니크 제약 충돌 방지)
public class TestDataCreator {
    private final ProductJpaRepository productRepository;
    private final StockJpaRepository stockJpaRepository;

    public TestDataCreator(ProductJpaRepository productRepository, StockJpaRepository stockJpaRepository) {
        this.productRepository = productRepository;
        this.stockJpaRepository = stockJpaRepository;
    }

    @PostConstruct
    public void createTestData() {
        Product product1 = new Product(100L);
        Product product2 = new Product(200L);
        productRepository.save(product1);
        productRepository.save(product2);

        Stock stock1 = new Stock(100L, 1L);
        Stock stock2 = new Stock(200L, 2L);

        stockJpaRepository.save(stock1);
        stockJpaRepository.save(stock2);
    }
}