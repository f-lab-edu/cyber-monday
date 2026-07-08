package brucehan.product.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;

/**
 * 대상 테이블(가짜/거울 - 양방향이 필요할 때만 추가)
 */
@Entity
@Getter
public class Stock {
    @Id @GeneratedValue
    private Long id;

    private int quantity;

    @OneToOne(mappedBy = "stock") // 주인이 아님
    private Product product;
}
