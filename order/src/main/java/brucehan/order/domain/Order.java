package brucehan.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static brucehan.order.domain.OrderStatus.*;

@Table(name = "orders")
@Entity
@Getter
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
    private OrderStatus status;

    public Order() {
        orderNumber = generateOrderNumber();
        status = AWAITING;
    }

    public void request() {
        if (status != AWAITING) throw new RuntimeException("잘못된 요청입니다.");
        status = PENDING;
    }


    public void approve(String paymentKey) {
        if (status != PENDING) throw new RuntimeException("잘못된 요청입니다.");
        this.paymentKey = paymentKey;
        paidAt = LocalDateTime.now();
        status = APPROVED;
    }

    public void complete() {
        if (status != APPROVED) throw new RuntimeException("잘못된 요청입니다.");
        status = COMPLETE;
    }

    public void failOrder() {
        if (status != APPROVED) throw new RuntimeException("잘못된 요청입니다.");
        status = ORDER_FAILED;
    }

    public void markUnknown() {
        status = UNKNOWN;
    }

    public void failPayment() {
        if (status != PENDING) throw new RuntimeException("잘못된 요청입니다.");
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
