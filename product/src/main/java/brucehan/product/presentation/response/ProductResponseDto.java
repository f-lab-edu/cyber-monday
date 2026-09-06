package brucehan.product.presentation.response;

import brucehan.product.domain.entity.Stock;

import java.time.LocalDateTime;

public record ProductResponseDto(
        Long id,
        Long price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
