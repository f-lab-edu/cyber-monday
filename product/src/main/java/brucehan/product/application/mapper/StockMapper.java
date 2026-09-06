package brucehan.product.application.mapper;

import brucehan.product.domain.entity.Stock;
import brucehan.product.presentation.response.StockResponseDto;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {
    public StockResponseDto toResponseDto(Stock stock) {
        return new StockResponseDto(stock.getQuantity(), stock.getProductId());
    }
}
