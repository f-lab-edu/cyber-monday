package brucehan.product.application;

import brucehan.product.application.dto.ProductPagedDto;
import brucehan.product.application.mapper.ProductMapper;
import brucehan.product.config.constant.ErrorCode;
import brucehan.product.config.exception.BusinessException;
import brucehan.product.domain.entity.Product;
import brucehan.product.domain.repository.ProductJpaRepository;
import brucehan.product.domain.repository.ProductQueryRepository;
import brucehan.product.presentation.request.ProductOffsetRequestDto;
import brucehan.product.presentation.response.ProductOffsetResponseDto;
import brucehan.product.presentation.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static brucehan.product.config.constant.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductQueryRepository productQueryRepository;
    private final ProductMapper productMapper;

    public Product findProductById(Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }

    public ProductResponseDto findProductDtoById(Long id) {
        return productMapper.toResponseDto(findProductById(id));
    }


    public ProductOffsetResponseDto<ProductPagedDto> getPagedProducts(ProductOffsetRequestDto request) {
        PageRequest pageRequest = PageRequest.of(request.pageNumber(), request.size());
        final Page<ProductPagedDto> content = productQueryRepository.findByOffset(pageRequest);

        return new ProductOffsetResponseDto<>(content.getContent(), content.getSize(), content.getNumber(), content.getTotalPages());
    }

    // 데이터 생성용
    @Transactional
    public void save(Product product) {
        productJpaRepository.save(product);
    }

}
