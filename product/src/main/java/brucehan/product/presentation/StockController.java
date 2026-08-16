package brucehan.product.presentation;

import brucehan.product.application.StockService;
import brucehan.product.presentation.request.StockRequestDto;
import brucehan.product.presentation.response.StockResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}")
    public StockResponseDto getStock(@PathVariable Long productId) {
        return stockService.getStock(productId);
    }

    @PatchMapping("/decrease")
    public void decreaseStock(
            @RequestBody final StockRequestDto stockRequestDto
    ) {
        stockService.decreaseStock(stockRequestDto);
    }

    @PatchMapping("/increase")
    public void increaseStock(
            @RequestBody final StockRequestDto stockRequestDto
    ) {
        stockService.increaseStock(stockRequestDto);
    }
}
