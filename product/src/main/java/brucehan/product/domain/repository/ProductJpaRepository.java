package brucehan.product.domain.repository;

import brucehan.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}
