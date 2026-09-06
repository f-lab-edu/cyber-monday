package brucehan.product.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ProductPagedDto(
        Long id,
        Long price
) {
    @QueryProjection
    public ProductPagedDto(Long id, Long price) {
        this.id = id;
        this.price = price;
    }
}
