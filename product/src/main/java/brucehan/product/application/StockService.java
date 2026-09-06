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

import static brucehan.product.config.constant.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private final StockJpaRepository stockJpaRepository;
    private final StockMapper stockMapper;

    // TODO : 멱등성 처리
    @Transactional
    public int decreaseStock(StockRequestDto stockRequestDto) {
        if (stockRequestDto.quantity() <= 0) throw new BusinessException(INVALID_REQUEST);
        Integer decreasedCount = stockJpaRepository.decrease(stockRequestDto.productId(), stockRequestDto.quantity());
        if (decreasedCount <= 0) { // 드라이버가 스펙 밖 음수를 반환할 가능성에 대비
            stockJpaRepository.findByProductId(stockRequestDto.productId()).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
            throw new BusinessException(STOCK_NOT_ENOUGH);
        }
        return decreasedCount;
    }

    @Transactional
    public int increaseStock(StockRequestDto stockRequestDto) {
        if (stockRequestDto.quantity() <= 0) throw new BusinessException(INVALID_REQUEST);
        Integer increasedCount = stockJpaRepository.increase(stockRequestDto.productId(), stockRequestDto.quantity());
        if (increasedCount <= 0) {
            stockJpaRepository.findByProductId(stockRequestDto.productId()).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        }
        return increasedCount;
    }

    @Transactional(readOnly = true)
    public StockResponseDto getStock(final Long productId) {
        Stock stock = stockJpaRepository.findByProductId(productId).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        return stockMapper.toResponseDto(stock);
    }
}
