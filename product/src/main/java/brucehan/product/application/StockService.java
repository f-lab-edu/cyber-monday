package brucehan.product.application;

import brucehan.product.application.mapper.StockMapper;
import brucehan.product.config.exception.BusinessException;
import brucehan.product.domain.entity.Stock;
import brucehan.product.domain.repository.StockJpaRepository;
import brucehan.product.presentation.request.StockRequestDto;
import brucehan.product.presentation.response.StockResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static brucehan.product.config.constant.ErrorCode.PRODUCT_NOT_FOUND;
import static brucehan.product.config.constant.ErrorCode.STOCK_NOT_ENOUGH;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private final StockJpaRepository stockJpaRepository;
    private final StockMapper stockMapper;

    // TODO : 멱등성 처리
    @Transactional
    public void decreaseStock(StockRequestDto stockRequestDto) {
        Integer decreasedCount = stockJpaRepository.decrease(stockRequestDto.productId(), stockRequestDto.quantity());
        if (decreasedCount == 0) {
            stockJpaRepository.findByProductId(stockRequestDto.productId()).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
            throw new BusinessException(STOCK_NOT_ENOUGH);
        }
    }

    @Transactional
    public void increaseStock(StockRequestDto stockRequestDto) {
        Integer increasedCount = stockJpaRepository.increase(stockRequestDto.productId(), stockRequestDto.quantity());
        if (increasedCount == 0) {
            stockJpaRepository.findByProductId(stockRequestDto.productId()).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        }
    }

    @Transactional(readOnly = true)
    public StockResponseDto getStock(Long productId) {
        Stock stock = stockJpaRepository.findByProductId(productId).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        return stockMapper.toResponseDto(stock);
    }
}
