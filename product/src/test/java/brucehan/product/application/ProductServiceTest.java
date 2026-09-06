package brucehan.product.application;

import brucehan.product.application.dto.ProductPagedDto;
import brucehan.product.domain.entity.Product;
import brucehan.product.presentation.request.ProductOffsetRequestDto;
import brucehan.product.presentation.response.ProductOffsetResponseDto;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    ProductService productService;
    @Autowired
    EntityManager em;

    @BeforeEach
    void setup() {
        em.persist(new Product(100L));
        em.persist(new Product(200L));
        em.persist(new Product(300L));
        em.persist(new Product(400L));
    }

    @Test
    void testGetPagedProducts() {
        // given
        ProductOffsetRequestDto request = new ProductOffsetRequestDto(0, 5);
        ProductOffsetResponseDto<ProductPagedDto> pagedProducts = productService.getPagedProducts(request);
        log.info("{} {}", pagedProducts.currentPage(), pagedProducts.size());
        assertThat(pagedProducts.content().size()).isEqualTo(4);
    }

    @Test
    void testGetEmptyPaged() {
        em.createQuery("delete from Product").executeUpdate();
        em.flush();
        ProductOffsetRequestDto request = new ProductOffsetRequestDto(0, 5);
        ProductOffsetResponseDto<ProductPagedDto> pagedProducts = productService.getPagedProducts(request);
        log.info("{} {}", pagedProducts.currentPage(), pagedProducts.size());
        assertThat(pagedProducts.content().size()).isEqualTo(0);
    }
}