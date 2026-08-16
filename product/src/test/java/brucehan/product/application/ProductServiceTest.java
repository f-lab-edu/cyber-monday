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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    ProductService productService;
    @Autowired
    EntityManager em;

    @BeforeEach
    void setup() {
        em.persist(new Product("티셔츠1", "기능성", "무탠다드", "무신사", 23500));
        em.persist(new Product("티셔츠2", "기능성", "무탠다드", "무신사", 23500));
        em.persist(new Product("티셔츠3", "기능성", "무탠다드", "무신사", 23500));
        em.persist(new Product("티셔츠4", "기능성", "무탠다드", "무신사", 23500));
    }

    @Test
    void testGetPagedProducts() {
        // given
        ProductOffsetRequestDto request = new ProductOffsetRequestDto(0, 5);
        ProductOffsetResponseDto<ProductPagedDto> pagedProducts = productService.getPagedProducts(request);
        log.info("{} {} {}", pagedProducts.content().get(0).name(), pagedProducts.currentPage(), pagedProducts.size());
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