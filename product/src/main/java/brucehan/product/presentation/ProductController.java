package brucehan.product.presentation;

import brucehan.product.application.ProductService;
import brucehan.product.presentation.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/v1/products/{productId}")
    public ResponseEntity<ProductResponse> findProduct() {
        return null;
    }

    @GetMapping("/v1/products")
    public ResponseEntity<List<ProductResponse>> findAllProducts() {
        return null;
    }
}
