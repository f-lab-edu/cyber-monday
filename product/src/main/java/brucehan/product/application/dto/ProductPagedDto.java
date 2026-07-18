package brucehan.product.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ProductPagedDto {
    private Long id;
    private String name;
    private Integer price;

    @QueryProjection
    public ProductPagedDto(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
