package brucehan.product.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

import static jakarta.persistence.GenerationType.*;

/**
 * 대상 테이블(가짜/거울 - 양방향이 필요할 때만 추가)
 */
@Entity
@Getter
public class Stock {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private int quantity;

    @OneToOne(mappedBy = "stock") // 주인이 아님
    private Product product;
}
