package brucehan.order.application;

import brucehan.order.config.exception.BusinessException;
import brucehan.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

    @Test
    void approveTest() {
        Order order = new Order();
        assertThatThrownBy(() -> order.approve("11234")).isInstanceOf(BusinessException.class);
    }
}
