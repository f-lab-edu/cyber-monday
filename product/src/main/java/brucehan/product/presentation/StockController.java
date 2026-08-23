package brucehan.product.presentation;

import brucehan.product.application.StockService;
import brucehan.product.presentation.request.StockRequestDto;
import brucehan.product.presentation.response.StockResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}")
    public ResponseEntity<StockResponseDto> getStock(@PathVariable final Long productId) {
        return ResponseEntity.ok(stockService.getStock(productId));
    }

    @PatchMapping("/decrease")
    public ResponseEntity<Integer> decreaseStock(
            @Valid @RequestBody final StockRequestDto stockRequestDto
    ) {
    int decreased = stockService.decreaseStock(stockRequestDto);
        return ResponseEntity.ok(decreased);
    }

    @PatchMapping("/increase")
    public ResponseEntity<Integer> increaseStock(
            @Valid @RequestBody final StockRequestDto stockRequestDto
    ) {
        int increased = stockService.increaseStock(stockRequestDto);
        return ResponseEntity.ok(increased);
    }
}
