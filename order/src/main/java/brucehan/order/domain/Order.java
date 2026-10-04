package brucehan.order.domain;

import brucehan.order.config.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static brucehan.order.config.constant.ErrorCode.INVALID_ORDER_STATE;
import static brucehan.order.domain.OrderStatus.*;

@Table(name = "orders")
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    private Long memberId;

    private Long productAmount;
    private Long discountAmount;
    private Long totalAmount;

    private String paymentKey;

    private LocalDateTime paidAt;

    private String failReason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(30)")
    private OrderStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Order() {
        orderNumber = generateOrderNumber();
        status = AWAITING;
    }

    public void request() {
        if (status != AWAITING) throw new BusinessException(INVALID_ORDER_STATE);
        status = PENDING;
    }

    public void approve(String paymentKey) {
        if (status != PENDING) throw new BusinessException(INVALID_ORDER_STATE);
        this.paymentKey = paymentKey;
        paidAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = APPROVED;
    }

    public void complete() {
        if (status != APPROVED) throw new BusinessException(INVALID_ORDER_STATE);
        status = COMPLETE;
    }

    public void failOrder() {
        if (status != APPROVED) throw new BusinessException(INVALID_ORDER_STATE);
        status = ORDER_FAILED;
    }

    public void failPayment() {
        if (status != AWAITING || status != PENDING) throw new BusinessException(INVALID_ORDER_STATE);
        status = PAYMENT_FAILED;
    }

    public Order(
            Long memberId,
            Long productAmount,
            Long discountAmount
    ) {
        this.orderNumber = generateOrderNumber();
        this.memberId = memberId;
        this.productAmount = defaultAmount(productAmount);
        this.discountAmount = defaultAmount(discountAmount);
        this.totalAmount = calculateAmount(defaultAmount(productAmount), defaultAmount(discountAmount));
        this.status = AWAITING;
    }

    private Long defaultAmount(Long amount) {
        return amount == null ? 0L : amount;
    }

    private Long calculateAmount(Long productAmount, Long discountAmount) {
        return productAmount - discountAmount;
    }


    private static String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + suffix;
    }
}
