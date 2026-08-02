package brucehan.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.*;

/**
 * 대상 테이블(가짜/거울 - 양방향이 필요할 때만 추가)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Integer quantity;

    @Column(name = "product_id", unique = true)
    private Long productId;

    public Stock(Integer quantity, Long productId) {
        this.quantity = quantity;
        this.productId = productId;
    }
}
