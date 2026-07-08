package brucehan.product.application;

import brucehan.product.application.mapper.ProductMapper;
import brucehan.product.domain.entity.Product;
import brucehan.product.domain.repository.ProductJpaRepository;
import brucehan.product.presentation.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
//    private final ProductQueryRepository productQueryRepository;
    private final ProductMapper productMapper;

    public Product findProductById(Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new RuntimeException());
    }

    public ProductResponse findProductDtoById(Long id) {
        return productMapper.toResponse(findProductById(id));
    }

}
