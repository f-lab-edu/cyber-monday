package brucehan.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Table(name = "compensation_registry")
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CompensationRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private CompensationRegistryStatus status;

    public CompensationRegistry(String orderNumber) {
        this.orderNumber = orderNumber;
        this.status = CompensationRegistryStatus.PENDING;
    }

    private enum CompensationRegistryStatus {
        PENDING, COMPLETE
    }
}
