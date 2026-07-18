package brucehan.product.presentation;

import brucehan.product.application.ProductService;
import brucehan.product.application.dto.ProductPagedDto;
import brucehan.product.presentation.request.ProductOffsetRequestDto;
import brucehan.product.presentation.response.ProductOffsetResponseDto;
import brucehan.product.presentation.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping("/v1/products/{id}")
    public ProductResponseDto findProductById(@PathVariable final Long id) {
        return productService.findProductDtoById(id);
    }

    @GetMapping("/v1/products/")
    public ProductOffsetResponseDto<ProductPagedDto> getPagedProducts(
            @RequestParam(required = false) final int offset,
            @RequestParam(required = false) final int limit,
            @RequestParam(defaultValue = "10") final int size
    ) {
        ProductOffsetRequestDto request = new ProductOffsetRequestDto(offset, limit, size);
        return productService.getPagedProducts(request);
    }
}
