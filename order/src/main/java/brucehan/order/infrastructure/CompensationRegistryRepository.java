package brucehan.order.infrastructure;

import brucehan.order.domain.CompensationRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRegistryRepository extends JpaRepository<CompensationRegistry, Long> {

    // TODO 메서드 만들어야 함
}
