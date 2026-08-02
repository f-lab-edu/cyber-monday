package brucehan.product.presentation;

import brucehan.product.application.StockService;
import brucehan.product.presentation.request.StockRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}")
    public Integer getStock(@PathVariable Long productId) {
        return stockService.getStock(productId);
    }

    @PatchMapping("/decrease")
    public String decreaseStock(
            @RequestBody final StockRequestDto stockRequestDto
    ) {
        return stockService.decreaseStockAndPublish(stockRequestDto);
    }

    @PatchMapping("/increase")
    public String increaseStock(
            @RequestBody final StockRequestDto stockRequestDto
    ) {
        return stockService.increaseStock(stockRequestDto);
    }
}
