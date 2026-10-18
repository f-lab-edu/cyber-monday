package brucehan.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "compensation_registry")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CompensationRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    private String failReason;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(30)")
    private CompensationRegistryStatus status;

    public CompensationRegistry(String orderNumber, String failReason) {
        this.orderNumber = orderNumber;
        this.status = CompensationRegistryStatus.PENDING;
        this.failReason = failReason;
    }

    public enum CompensationRegistryStatus {
        PENDING, COMPLETE
    }
}
