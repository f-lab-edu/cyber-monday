package brucehan.product.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ProductPagedDto(
        Long id,
        String name,
        Integer price
) {
    @QueryProjection
    public ProductPagedDto(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
