package brucehan.product.presentation;

import brucehan.product.application.ProductService;
import brucehan.product.application.RedisLockService;
import brucehan.product.application.dto.ProductBuyCancelResultDto;
import brucehan.product.application.dto.ProductBuyResultDto;
import brucehan.product.application.dto.ProductPagedDto;
import brucehan.product.presentation.request.ProductBuyCancelRequestDto;
import brucehan.product.presentation.request.ProductBuyRequestDto;
import brucehan.product.presentation.request.ProductOffsetRequestDto;
import brucehan.product.presentation.response.ProductBuyCancelResponseDto;
import brucehan.product.presentation.response.ProductBuyResponseDto;
import brucehan.product.presentation.response.ProductOffsetResponseDto;
import brucehan.product.presentation.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RedisLockService redisLockService;

    @GetMapping("/v1/products/{id}")
    public ProductResponseDto findProductById(@PathVariable final Long id) {
        return productService.findProductDtoById(id);
    }

    @GetMapping("/v1/products")
    public ProductOffsetResponseDto<ProductPagedDto> getPagedProducts(
            @RequestParam(defaultValue = "0") final int pageNumber,
            @RequestParam(defaultValue = "10") final int size
    ) {
        ProductOffsetRequestDto request = new ProductOffsetRequestDto(pageNumber, size);
        return productService.getPagedProducts(request);
    }

    @PostMapping("/v1/products/buy")
    public ProductBuyResponseDto buy(@RequestBody ProductBuyRequestDto request) {
        String lockKey = "cybermonday:product:" + request.requestId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.requestId());
        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }
        try {
            ProductBuyResultDto buyResult = productService.buy(request.toCommand());
            return new ProductBuyResponseDto(buyResult.totalPrice());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @PostMapping("/v1/products/buy/cancel")
    public ProductBuyCancelResponseDto cancel(@RequestBody ProductBuyCancelRequestDto request) {
        String lockKey = "cybermonday:product:" + request.requestId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.requestId());
        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }

        try {
            ProductBuyCancelResultDto cancelResult = productService.cancel(request.toCommand());
            return new ProductBuyCancelResponseDto(cancelResult.totalPrice());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }
}
