package brucehan.product.presentation;

import brucehan.product.application.ProductService;
import brucehan.product.presentation.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping("/v1/products/{id}")
    public ProductResponse findProductById(@PathVariable final Long id) {
        return productService.findProductDtoById(id);
    }

    @GetMapping("/v1/products/{productId}")
    public ProductResponse getPagedProducts() {

        return null;
    }
}
