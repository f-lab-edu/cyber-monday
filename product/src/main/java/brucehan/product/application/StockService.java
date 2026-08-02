package brucehan.product.application;

import brucehan.product.config.constant.ErrorCode;
import brucehan.product.config.exception.BusinessException;
import brucehan.product.domain.repository.StockJpaRepository;
import brucehan.product.presentation.request.StockRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static brucehan.product.config.constant.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private final StockJpaRepository stockJpaRepository;

    // TODO : 멱등성 처리
    @Transactional
    public String decreaseStockAndPublish(StockRequestDto stockRequestDto) {
        Integer decreased = stockJpaRepository.decrease(stockRequestDto.productId(), stockRequestDto.quantity());
        if (decreased == 0) {
            log.info("재고 부족, 품절 - 상품 ID : {}", stockRequestDto.productId());
            return "soldout";
        }
        return "success";
    }

    @Transactional
    public String increaseStock(StockRequestDto stockRequestDto) {
        Integer increased = stockJpaRepository.increase(stockRequestDto.productId(), stockRequestDto.quantity());
        if (increased == 0) {
            throw new BusinessException(STOCK_NOT_ENOUGH);
        }
        return "success";
    }

    @Transactional(readOnly = true)
    public Integer getStock(Long productId) {
        Optional<Integer> quantityByProductId = stockJpaRepository.findQuantityByProductId(productId);
        if (quantityByProductId.isPresent()) {
            return quantityByProductId.get();
        } else {
            throw new BusinessException(PRODUCT_NOT_FOUND); // 404 not found
        }
    }
}
