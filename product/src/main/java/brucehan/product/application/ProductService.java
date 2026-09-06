package brucehan.product.application;

import brucehan.product.application.dto.*;
import brucehan.product.application.mapper.ProductMapper;
import brucehan.product.config.exception.BusinessException;
import brucehan.product.domain.entity.Product;
import brucehan.product.domain.entity.ProductTransactionHistory;
import brucehan.product.domain.entity.Stock;
import brucehan.product.infrastructure.ProductJpaRepository;
import brucehan.product.infrastructure.ProductQueryRepository;
import brucehan.product.infrastructure.ProductTransactionHistoryRepository;
import brucehan.product.presentation.request.ProductOffsetRequestDto;
import brucehan.product.presentation.request.StockRequestDto;
import brucehan.product.presentation.response.ProductOffsetResponseDto;
import brucehan.product.presentation.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static brucehan.product.config.constant.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductQueryRepository productQueryRepository;
    private final ProductMapper productMapper;
    private final StockService stockService;
    private final ProductTransactionHistoryRepository productTransactionHistoryRepository;

    public Product findProductById(Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }

    public ProductResponseDto findProductDtoById(Long id) {
        return productMapper.toResponseDto(findProductById(id));
    }


    public ProductOffsetResponseDto<ProductPagedDto> getPagedProducts(ProductOffsetRequestDto request) {
        PageRequest pageRequest = PageRequest.of(request.pageNumber(), request.size());
        final Page<ProductPagedDto> content = productQueryRepository.findByOffset(pageRequest);

        return new ProductOffsetResponseDto<>(content.getContent(), content.getSize(), content.getNumber(), content.getTotalPages());
    }

    // 데이터 생성용
    @Transactional
    public void save(Product product) {
        productJpaRepository.save(product);
    }

    @Transactional
    public ProductBuyResultDto buy(ProductBuyCommandDto command) {
        List<ProductTransactionHistory> histories = productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
                command.requestId(),
                ProductTransactionHistory.TransactionType.PURCHASE
        );

        if (!histories.isEmpty()) {
            log.warn("이미 구매한 이력이 있습니다.");

            long totalPrice = histories.stream()
                    .mapToLong(ProductTransactionHistory::getPrice)
                    .sum();
            return new ProductBuyResultDto(totalPrice);
        }

        Long totalPrice = 0L;

        for (ProductBuyCommandDto.ProductInfo productInfo : command.productInfos()) {
            Product product = productJpaRepository.findById(productInfo.productId()).orElseThrow();
            stockService.decreaseStock(new StockRequestDto(productInfo.productId(), productInfo.quantity()));

            Long price = product.calculatePrice(new Stock(productInfo.quantity(), productInfo.productId()));
            totalPrice += price;

            productTransactionHistoryRepository.save(
                    new ProductTransactionHistory(
                            command.requestId(),
                            productInfo.productId(),
                            productInfo.quantity(),
                            price,
                            ProductTransactionHistory.TransactionType.PURCHASE
                    )
            );
        }
        return new ProductBuyResultDto(totalPrice);
    }

    @Transactional
    public ProductBuyCancelResultDto cancel(ProductBuyCancelCommandDto command) {
        List<ProductTransactionHistory> buyHistories = productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
                command.requestId(),
                ProductTransactionHistory.TransactionType.PURCHASE
        );

        if (buyHistories.isEmpty()) {
            return new ProductBuyCancelResultDto(0L);
        }

        List<ProductTransactionHistory> cancelHistories = productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
                command.requestId(),
                ProductTransactionHistory.TransactionType.CANCEL
        );

        if (!cancelHistories.isEmpty()) {
            log.warn("이미 취소되었습니다.");
            Long totalPrice = cancelHistories.stream()
                    .mapToLong(ProductTransactionHistory::getPrice)
                    .sum();
            return new ProductBuyCancelResultDto(totalPrice);
        }

        Long totalPrice = 0L;

        for (ProductTransactionHistory history : buyHistories) {
            stockService.increaseStock(new StockRequestDto(history.getProductId(), history.getQuantity()));
            totalPrice += history.getPrice();

            productTransactionHistoryRepository.save(
                    new ProductTransactionHistory(
                            command.requestId(),
                            history.getProductId(),
                            history.getQuantity(),
                            history.getPrice(),
                            ProductTransactionHistory.TransactionType.CANCEL
                    )
            );
        }
        return new ProductBuyCancelResultDto(totalPrice);
    }

}
