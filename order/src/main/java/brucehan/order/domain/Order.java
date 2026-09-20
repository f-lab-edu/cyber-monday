package brucehan.order.domain;

import jakarta.persistence.*;

import static brucehan.order.domain.Order.OrderStatus.*;

@Table(name = "orders")
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order() {
        status = CREATED;
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void request() {
        if (status != CREATED) throw new RuntimeException("잘못된 요청입니다.");
        status = REQUESTED;
    }

    public void complete() {
        status = COMPLETED;
    }

    public void fail() {
        if (status != REQUESTED) throw new RuntimeException("잘못된 요청입니다.");
        status = FAILED;
    }

    public enum OrderStatus {
        CREATED, REQUESTED, FAILED, COMPLETED
    }
}
