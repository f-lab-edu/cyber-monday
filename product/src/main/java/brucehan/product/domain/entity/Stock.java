package brucehan.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock")
public class Stock {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long quantity;

    @Column(name = "product_id", unique = true)
    private Long productId;

    public Stock(Long quantity, Long productId) {
        this.quantity = quantity;
        this.productId = productId;
    }
}
