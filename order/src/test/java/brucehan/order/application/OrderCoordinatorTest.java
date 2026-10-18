package brucehan.order.application;

import brucehan.order.application.dto.OrderItemDto;
import brucehan.order.application.dto.PlaceOrderCommand;
import brucehan.order.config.exception.BusinessException;
import brucehan.order.domain.CompensationRegistry;
import brucehan.order.infrastructure.CompensationRegistryRepository;
import brucehan.order.infrastructure.product.ProductApiClient;
import brucehan.order.infrastructure.product.dto.ProductBuyApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyApiResponse;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiRequest;
import brucehan.order.infrastructure.product.dto.ProductBuyCancelApiResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static brucehan.order.config.constant.ErrorCode.*;
import static brucehan.order.domain.CompensationRegistry.CompensationRegistryStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderCoordinatorTest {

    @Mock
    OrderService orderService;
    @Mock
    ProductApiClient productApiClient;
    @Mock
    CompensationRegistryRepository compensationRegistryRepository;

    @InjectMocks
    OrderCoordinator orderCoordinator;

    /*
     * 승인완료 -> 재고 부족 -> 주문실패 (+ 승인 취소)
     * 코디네이터가 "무엇을 호출했는가"를 검증한다. 상태 전이 자체는 Order 도메인 테스트의 몫.
     */
    @Test
    void rollbackWhenProductBuyFails() {
        // given
        String orderNumber = "ORD-20260928-TEST0001";
        PlaceOrderCommand command = new PlaceOrderCommand(orderNumber);

        given(orderService.getOrder(orderNumber))
                .willReturn(new OrderItemDto(List.of(new OrderItemDto.OrderItem(1L, 2L))));
        given(productApiClient.buy(any(ProductBuyApiRequest.class)))
                .willThrow(new RuntimeException("재고 부족"));
        given(productApiClient.cancel(any(ProductBuyCancelApiRequest.class)))
                .willReturn(new ProductBuyCancelApiResponse(0L));

        // when & then : 예외를 삼키지 않고 호출자에게 그대로 올려야 한다
        assertThatThrownBy(() -> orderCoordinator.placeOrder(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고 부족");

        // then : 보상 트랜잭션이 정확한 주문번호로 1회 실행됐는가
        then(productApiClient).should().cancel(new ProductBuyCancelApiRequest(orderNumber));
        then(orderService).should().failOrder(orderNumber);
        then(orderService).should(never()).complete(orderNumber);
        then(compensationRegistryRepository).shouldHaveNoInteractions();
    }

    @Test
    void registerCompensationWhenRollbackAlsoFails() {
        String orderNumber = "ORD-20260928-TEST0001";
        PlaceOrderCommand command = new PlaceOrderCommand(orderNumber);

        given(orderService.getOrder(orderNumber))
                .willReturn(new OrderItemDto(List.of(new OrderItemDto.OrderItem(1L, 2L))));

        // given : buy() 도 실패하고, 보상하려던 cancel() 마저 실패하는 상황
        given(productApiClient.buy(any(ProductBuyApiRequest.class)))
                .willThrow(new RuntimeException("재고 부족"));
        given(productApiClient.cancel(any(ProductBuyCancelApiRequest.class)))
                .willThrow(new RuntimeException("cancel 실패")); // APPROVED, cancel되기 전에 실패함

        // when  : placeOrder 호출
        assertThatThrownBy(() -> orderCoordinator.placeOrder(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고 부족");

        // then  : compensationRegistryRepository.save(...) 가 호출되는가?
        ArgumentCaptor<CompensationRegistry> captor = ArgumentCaptor.forClass(CompensationRegistry.class);
        then(compensationRegistryRepository).should().save(captor.capture());
        // then  : 이때 failOrder 는 호출되는가? 호출 안 되면 주문 상태는 뭐로 남는가? -> rollback()의 throw e;(61번 줄), APPROVED
        then(orderService).should(never()).failOrder(orderNumber);
        // then  : 호출자에게 전파되는 예외는 "재고 부족" 인가 "cancel 실패" 인가?
        assertThat(captor.getValue()).extracting("orderNumber").isEqualTo(orderNumber);
        assertThat(captor.getValue()).extracting("status").isEqualTo(PENDING);
    }


    @Test
    void rethrowsBusinessExceptionAfterRollback() {
        // given
        String orderNumber = "ORD-20260928-TEST0001";
        PlaceOrderCommand command = new PlaceOrderCommand(orderNumber);

        given(orderService.getOrder(orderNumber))
                .willReturn(new OrderItemDto(List.of(new OrderItemDto.OrderItem(1L, 2L))));
        given(productApiClient.buy(any(ProductBuyApiRequest.class)))
                .willThrow(new BusinessException(STOCK_NOT_ENOUGH));
        given(productApiClient.cancel(any(ProductBuyCancelApiRequest.class)))
                .willReturn(new ProductBuyCancelApiResponse(0L));

        // when & then : 예외를 삼키지 않고 호출자에게 그대로 올려야 한다
        assertThatThrownBy(() -> orderCoordinator.placeOrder(command))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(STOCK_NOT_ENOUGH));

        // then : 보상 트랜잭션이 정확한 주문번호로 1회 실행됐는가
        then(productApiClient).should().cancel(new ProductBuyCancelApiRequest(orderNumber));
        then(orderService).should().failOrder(orderNumber);
        then(orderService).should(never()).complete(orderNumber);
        then(compensationRegistryRepository).shouldHaveNoInteractions();
    }


    @Test
    void noRollbackWhenRequestFails() {
        // given : orderService.request() 가 예외를 던지는 상황 (중복 요청 / 잘못된 상태)
        String orderNumber = "ORD-20260928-TEST0001";
        PlaceOrderCommand command = new PlaceOrderCommand(orderNumber);

        given(orderService.getOrder(orderNumber))
                .willThrow(new BusinessException(ORDER_NOT_FOUND));

        // when  : placeOrder 호출
        assertThatThrownBy(() -> orderCoordinator.placeOrder(command))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ORDER_NOT_FOUND));

        // then  : productApiClient 는 아예 건드리면 안 된다 (shouldHaveNoInteractions)
        then(productApiClient).shouldHaveNoInteractions();

        // 왜 rollback 이 돌면 안 되는지 한 줄로 적어보세요.
        System.out.println("아직 결제 전이기 때문에 rollback은 되지 않음");
    }

    @Test
    void rollbackWhenCompleteFails() {
        // given : buy() 는 성공했는데 complete() 가 실패하는 상황
        String orderNumber = "ORD-20260928-TEST0001";
        PlaceOrderCommand command = new PlaceOrderCommand(orderNumber);

        given(orderService.getOrder(orderNumber))
                .willReturn(new OrderItemDto(List.of(new OrderItemDto.OrderItem(1L, 2L))));
        given(productApiClient.buy(any(ProductBuyApiRequest.class)))
                .willReturn(new ProductBuyApiResponse(200L));
        willThrow(new BusinessException(INVALID_ORDER_STATE))
                .given(orderService).complete(orderNumber);

        // when  : placeOrder 호출
        assertThatThrownBy(() -> orderCoordinator.placeOrder(command))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(INVALID_ORDER_STATE));

        // then  : 이미 차감된 재고는 어떻게 되어야 하는가?
        then(productApiClient).should().cancel(new ProductBuyCancelApiRequest(orderNumber));
    }

    @Test
    void cancelResponseWithNullTotalPrice() {
        // given : cancel() 이 ProductBuyCancelApiResponse(null) 을 돌려주는 상황
        String orderNumber = "ORD-20260928-TEST0001";
        PlaceOrderCommand command = new PlaceOrderCommand(orderNumber);

        given(orderService.getOrder(orderNumber))
                .willReturn(new OrderItemDto(List.of(new OrderItemDto.OrderItem(1L, 2L))));
        given(productApiClient.buy(any(ProductBuyApiRequest.class)))
                .willReturn(new ProductBuyApiResponse(200L));
        given(productApiClient.cancel(any(ProductBuyCancelApiRequest.class)))
                .willReturn(new ProductBuyCancelApiResponse(null));

        willThrow(new BusinessException(INVALID_ORDER_STATE))
                .given(orderService).complete(orderNumber);

        // when  : placeOrder 호출
        assertThatThrownBy(() -> orderCoordinator.placeOrder(command))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ORDER_IN_PROGRESS));

        // then  : OrderCoordinator.rollback() 의 totalPrice() > 0 에서 무슨 일이 일어나는가?
//        then(orderCoordinator).shouldHaveNoMoreInteractions();

        // 결과적으로 failOrder 는 호출되는가?
        then(orderService).should(never()).failOrder(orderNumber);
    }
}
