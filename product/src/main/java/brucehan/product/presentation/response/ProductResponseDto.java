package brucehan.product.presentation.response;

import brucehan.product.domain.entity.Stock;

import java.time.LocalDateTime;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        String brandName,
        String seller,
        Integer price,
        Long stockId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
