package brucehan.order.application;

import brucehan.order.application.dto.OrderItemDto;
import brucehan.order.application.dto.PlaceOrderCommand;
import brucehan.order.config.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static brucehan.order.config.constant.ErrorCode.ORDER_NOT_FOUND;
import static brucehan.order.config.constant.ErrorCode.STOCK_NOT_ENOUGH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderService orderService;

    @Test
    void getOrderTest() {
        // given
        String orderNumber = "ORD-20260928-TEST0001";

        given(orderService.getOrder(orderNumber))
                .willThrow(new BusinessException(ORDER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(orderNumber))
                .isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ORDER_NOT_FOUND));
    }

}