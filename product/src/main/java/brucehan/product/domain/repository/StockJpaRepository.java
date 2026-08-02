package brucehan.product.domain.repository;

import brucehan.product.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s.quantity FROM Stock s WHERE s.productId = :productId")
    Optional<Integer> findQuantityByProductId(Long productId);

    @Query("UPDATE Stock s SET s.quantity = s.quantity - :quantity WHERE s.productId = :productId AND s.quantity >= :quantity")
    @Modifying
    Integer decrease(Long productId, Integer quantity);

    @Query("UPDATE Stock s SET s.quantity = s.quantity + :quantity WHERE s.productId = :productId")
    @Modifying
    Integer increase(Long productId, Integer quantity);
}
