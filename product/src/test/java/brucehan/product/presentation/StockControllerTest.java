package brucehan.product.presentation;

import brucehan.product.domain.entity.Product;
import brucehan.product.domain.entity.Stock;
import brucehan.product.infrastructure.ProductJpaRepository;
import brucehan.product.infrastructure.ProductTransactionHistoryRepository;
import brucehan.product.infrastructure.StockJpaRepository;
import brucehan.product.presentation.request.StockRequestDto;
import brucehan.product.support.ControllerIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StockController 경계값 통합 테스트.
 * <p>
 * 재고 수량의 경계 : 0 / 1 / 보유수량 - 1 / 보유수량 / 보유수량 + 1
 */
@DisplayName("StockController 경계값 통합 테스트")
class StockControllerTest extends ControllerIntegrationTest {

    private static final long INITIAL_QUANTITY = 100L;

    @Autowired
    StockJpaRepository stockJpaRepository;
    @Autowired
    ProductJpaRepository productJpaRepository;
    @Autowired
    ProductTransactionHistoryRepository productTransactionHistoryRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        clearAll();
        Product product = productJpaRepository.save(new Product(1_000L));
        productId = product.getId();
        stockJpaRepository.save(new Stock(INITIAL_QUANTITY, productId));
    }

    @AfterEach
    void tearDown() {
        clearAll();
    }

    private void clearAll() {
        productTransactionHistoryRepository.deleteAll();
        stockJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
    }

    private long currentQuantity() {
        return stockJpaRepository.findByProductId(productId).orElseThrow().getQuantity();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Nested
    @DisplayName("GET /v1/stocks/{productId}")
    class GetStock {

        @Test
        @DisplayName("재고가 있는 상품이면 200과 수량을 반환한다")
        void existingProduct() throws Exception {
            mockMvc.perform(get("/v1/stocks/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.quantity").value(INITIAL_QUANTITY))
                    .andExpect(jsonPath("$.productId").value(productId));
        }

        @Test
        @DisplayName("재고 행이 없는 상품이면 404를 반환한다")
        void notExistingProduct() throws Exception {
            mockMvc.perform(get("/v1/stocks/{productId}", productId + 1))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("productId 하한 경계 - 0이면 404를 반환한다")
        void zeroProductId() throws Exception {
            mockMvc.perform(get("/v1/stocks/{productId}", 0L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("productId 하한 경계 - 음수면 404를 반환한다")
        void negativeProductId() throws Exception {
            mockMvc.perform(get("/v1/stocks/{productId}", -1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("productId 상한 경계 - Long.MAX_VALUE도 404로 처리된다")
        void maxProductId() throws Exception {
            mockMvc.perform(get("/v1/stocks/{productId}", Long.MAX_VALUE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("[현재 동작] productId 타입이 Long 범위를 벗어나면 500을 반환한다")
        void overflowProductId() throws Exception {
            // Long.MAX_VALUE + 1 -> 타입 변환 실패. 400이 적절하지만 현재는 공통 Exception 핸들러가 500으로 처리한다.
            mockMvc.perform(get("/v1/stocks/{productId}", "9223372036854775808"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    @DisplayName("PATCH /v1/stocks/decrease")
    class DecreaseStock {

        @Test
        @DisplayName("수량 하한 경계 - 0이면 @Min 위반으로 400을 반환하고 재고는 그대로다")
        void quantityZero() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, 0L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("수량 하한 경계 - -1이면 400을 반환하고 재고는 그대로다")
        void quantityNegative() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, -1L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("수량 하한 경계 - 1이면 성공하고 재고가 1 줄어든다")
        void quantityOne() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, 1L))))
                    .andExpect(status().isOk())
                    // 응답 값은 차감된 수량이 아니라 update 된 행 수(1)다.
                    .andExpect(content().string("1"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY - 1);
        }

        @Test
        @DisplayName("수량 상한 경계 - 보유수량과 같으면 성공하고 재고가 0이 된다")
        void quantityEqualsStock() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, INITIAL_QUANTITY))))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1"));

            assertThat(currentQuantity()).isZero();
        }

        @Test
        @DisplayName("수량 상한 경계 - 보유수량 + 1이면 429를 반환하고 재고는 그대로다")
        void quantityOverStock() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, INITIAL_QUANTITY + 1))))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value("STOCK_NOT_ENOUGH"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("수량 상한 경계 - Long.MAX_VALUE면 429를 반환하고 재고는 그대로다")
        void quantityMaxValue() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, Long.MAX_VALUE))))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value("STOCK_NOT_ENOUGH"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("재고가 0인 상품을 1개 차감하면 429를 반환한다")
        void decreaseFromEmptyStock() throws Exception {
            stockJpaRepository.deleteAll();
            stockJpaRepository.save(new Stock(0L, productId));

            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, 1L))))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value("STOCK_NOT_ENOUGH"));

            assertThat(currentQuantity()).isZero();
        }

        @Test
        @DisplayName("productId가 null이면 @NotNull 위반으로 400을 반환한다")
        void nullProductId() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"productId\":null,\"quantity\":1}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404를 반환한다")
        void notExistingProduct() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId + 1, 1L))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("[현재 동작] quantity가 null이면 @Min을 통과해 언박싱 NPE로 500이 된다")
        void nullQuantity() throws Exception {
            // @Min 은 null 을 통과시키므로 StockRequestDto.quantity 에 @NotNull 이 필요하다.
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"productId\":" + productId + ",\"quantity\":null}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("[현재 동작] 본문이 비어 있으면 400이 아니라 500이 된다")
        void emptyBody() throws Exception {
            // HttpMessageNotReadableException(원래 400)까지 GlobalExceptionHandler 의 Exception 핸들러가 500으로 덮는다.
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    @DisplayName("PATCH /v1/stocks/increase")
    class IncreaseStock {

        @Test
        @DisplayName("수량 하한 경계 - 0이면 @Min 위반으로 400을 반환하고 재고는 그대로다")
        void quantityZero() throws Exception {
            mockMvc.perform(patch("/v1/stocks/increase")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, 0L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("수량 하한 경계 - 1이면 성공하고 재고가 1 늘어난다")
        void quantityOne() throws Exception {
            mockMvc.perform(patch("/v1/stocks/increase")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, 1L))))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY + 1);
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404를 반환한다")
        void notExistingProduct() throws Exception {
            mockMvc.perform(patch("/v1/stocks/increase")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId + 1, 1L))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("차감 후 동일 수량을 증가시키면 최초 수량으로 복원된다")
        void decreaseThenIncreaseRestores() throws Exception {
            mockMvc.perform(patch("/v1/stocks/decrease")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, INITIAL_QUANTITY))))
                    .andExpect(status().isOk());
            mockMvc.perform(patch("/v1/stocks/increase")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new StockRequestDto(productId, INITIAL_QUANTITY))))
                    .andExpect(status().isOk());

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }
    }
}
