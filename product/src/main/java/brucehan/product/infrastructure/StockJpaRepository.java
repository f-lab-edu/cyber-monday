package brucehan.product.infrastructure;

import brucehan.product.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

//    @Lock(PESSIMISTIC_WRITE) // 비관락을 쓰면 안 쓴 것에 비해 30% 느려짐. 동시성 제어 효과는 같음.
    @Query("SELECT s FROM Stock s WHERE s.productId = :productId")
    Optional<Stock> findByProductId(Long productId);

    @Query("UPDATE Stock s SET s.quantity = s.quantity - :quantity WHERE s.productId = :productId AND s.quantity >= :quantity")
    @Modifying
    Integer decrease(Long productId, Long quantity);

    @Query("UPDATE Stock s SET s.quantity = s.quantity + :quantity WHERE s.productId = :productId")
    @Modifying
    Integer increase(Long productId, Long quantity);
}
