package brucehan.product.presentation;

import brucehan.product.domain.entity.Product;
import brucehan.product.domain.entity.ProductTransactionHistory;
import brucehan.product.domain.entity.Stock;
import brucehan.product.infrastructure.ProductJpaRepository;
import brucehan.product.infrastructure.ProductTransactionHistoryRepository;
import brucehan.product.infrastructure.StockJpaRepository;
import brucehan.product.presentation.request.ProductBuyCancelRequestDto;
import brucehan.product.presentation.request.ProductBuyRequestDto;
import brucehan.product.support.ControllerIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductController 경계값 통합 테스트.
 * <p>
 * 페이징 경계 : size 0 / 1 / 전체개수, pageNumber 음수 / 마지막 / 마지막 + 1
 * 구매 경계 : 수량 0 / 1 / 재고 / 재고 + 1, 동일 requestId 재요청(멱등)
 */
@DisplayName("ProductController 경계값 통합 테스트")
class ProductControllerTest extends ControllerIntegrationTest {

    private static final long PRICE = 1_000L;
    private static final long INITIAL_QUANTITY = 100L;

    @Autowired
    ProductJpaRepository productJpaRepository;
    @Autowired
    StockJpaRepository stockJpaRepository;
    @Autowired
    ProductTransactionHistoryRepository productTransactionHistoryRepository;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private Long productId;

    @BeforeEach
    void setUp() {
        clearAll();
        Product product = productJpaRepository.save(new Product(PRICE));
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

    private ProductBuyRequestDto buyRequest(String requestId, long quantity) {
        return new ProductBuyRequestDto(
                requestId,
                List.of(new ProductBuyRequestDto.ProductInfo(productId, quantity))
        );
    }

    @Nested
    @DisplayName("GET /v1/products/{id}")
    class FindProductById {

        @Test
        @DisplayName("존재하는 상품이면 200과 상품 정보를 반환한다")
        void existingProduct() throws Exception {
            mockMvc.perform(get("/v1/products/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(productId))
                    .andExpect(jsonPath("$.price").value(PRICE))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404를 반환한다")
        void notExistingProduct() throws Exception {
            mockMvc.perform(get("/v1/products/{id}", productId + 1))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("id 하한 경계 - 0이면 404를 반환한다")
        void zeroId() throws Exception {
            mockMvc.perform(get("/v1/products/{id}", 0L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("id 하한 경계 - 음수면 404를 반환한다")
        void negativeId() throws Exception {
            mockMvc.perform(get("/v1/products/{id}", -1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("id 상한 경계 - Long.MAX_VALUE도 404로 처리된다")
        void maxId() throws Exception {
            mockMvc.perform(get("/v1/products/{id}", Long.MAX_VALUE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /v1/products")
    class GetPagedProducts {

        /** setUp 의 1건에 2건을 더해 총 3건으로 페이징 경계를 확인한다. */
        @BeforeEach
        void addProducts() {
            productJpaRepository.save(new Product(PRICE));
            productJpaRepository.save(new Product(PRICE));
        }

        @Test
        @DisplayName("파라미터가 없으면 기본값 pageNumber=0, size=10으로 조회한다")
        void defaultParameters() throws Exception {
            mockMvc.perform(get("/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("size 하한 경계 - 1이면 한 건만 조회되고 totalPages는 전체 건수와 같다")
        void sizeOne() throws Exception {
            mockMvc.perform(get("/v1/products").param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.size").value(1))
                    .andExpect(jsonPath("$.totalPages").value(3));
        }

        @Test
        @DisplayName("size 경계 - 전체 건수와 같으면 한 페이지에 모두 담긴다")
        void sizeEqualsTotalCount() throws Exception {
            mockMvc.perform(get("/v1/products").param("size", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("pageNumber 경계 - 마지막 페이지는 남은 건수만 반환한다")
        void lastPage() throws Exception {
            mockMvc.perform(get("/v1/products")
                            .param("pageNumber", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.currentPage").value(1))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @DisplayName("pageNumber 경계 - 마지막 페이지를 넘어서면 빈 목록을 반환한다")
        void beyondLastPage() throws Exception {
            mockMvc.perform(get("/v1/products")
                            .param("pageNumber", "2")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.currentPage").value(2));
        }

        @Test
        @DisplayName("상품이 하나도 없으면 빈 목록과 totalPages=0을 반환한다")
        void emptyProducts() throws Exception {
            clearAll();

            mockMvc.perform(get("/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Test
        @DisplayName("[현재 동작] size 하한 경계 - 0이면 PageRequest 예외로 500이 된다")
        void sizeZero() throws Exception {
            // PageRequest.of(page, 0) 이 IllegalArgumentException 을 던진다. 400으로 막는 편이 낫다.
            mockMvc.perform(get("/v1/products").param("size", "0"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }

        @Test
        @DisplayName("[현재 동작] pageNumber 하한 경계 - 음수면 PageRequest 예외로 500이 된다")
        void negativePageNumber() throws Exception {
            mockMvc.perform(get("/v1/products").param("pageNumber", "-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }

        @Test
        @DisplayName("[현재 동작] 숫자가 아닌 파라미터는 500이 된다")
        void notNumericParameter() throws Exception {
            mockMvc.perform(get("/v1/products").param("size", "abc"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    @DisplayName("POST /v1/products/buy")
    class Buy {

        @Test
        @DisplayName("수량 하한 경계 - 1이면 200과 단가 * 1을 반환하고 재고가 1 줄어든다")
        void quantityOne() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest("req-1", 1L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY - 1);
        }

        @Test
        @DisplayName("수량 상한 경계 - 재고와 같으면 200이고 재고가 0이 된다")
        void quantityEqualsStock() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest("req-max", INITIAL_QUANTITY))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE * INITIAL_QUANTITY));

            assertThat(currentQuantity()).isZero();
        }

        @Test
        @DisplayName("수량 상한 경계 - 재고 + 1이면 429이고 재고와 거래이력이 모두 롤백된다")
        void quantityOverStock() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest("req-over", INITIAL_QUANTITY + 1))))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value("STOCK_NOT_ENOUGH"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
            assertThat(productTransactionHistoryRepository.count()).isZero();
        }

        @Test
        @DisplayName("수량 하한 경계 - 0이면 400이고 재고는 그대로다")
        void quantityZero() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest("req-zero", 0L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("수량 하한 경계 - 음수면 400이고 재고는 그대로다")
        void quantityNegative() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest("req-negative", -1L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("동일 requestId로 재요청하면 멱등하게 같은 금액을 반환하고 재고는 한 번만 줄어든다")
        void sameRequestIdIsIdempotent() throws Exception {
            String requestId = "req-idempotent";

            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest(requestId, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE * 10));
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest(requestId, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE * 10));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY - 10);
            assertThat(productTransactionHistoryRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("productInfos가 비어 있으면 200과 총액 0을 반환한다")
        void emptyProductInfos() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ProductBuyRequestDto("req-empty", List.of()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(0));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("이미 락이 잡혀 있으면 500을 반환하고 재고는 그대로다")
        void lockAlreadyHeld() throws Exception {
            String requestId = "req-locked";
            String lockKey = "cybermonday:product:" + requestId;
            stringRedisTemplate.opsForValue().set(lockKey, requestId);
            try {
                mockMvc.perform(post("/v1/products/buy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(buyRequest(requestId, 1L))))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
            } finally {
                stringRedisTemplate.delete(lockKey);
            }

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("정상 요청이 끝나면 락 키가 남지 않는다")
        void lockIsReleased() throws Exception {
            String requestId = "req-release";

            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest(requestId, 1L))))
                    .andExpect(status().isOk());

            assertThat(stringRedisTemplate.hasKey("cybermonday:product:" + requestId)).isFalse();
        }

        @Test
        @DisplayName("[현재 동작] 존재하지 않는 상품이면 404가 아니라 500이 된다")
        void notExistingProduct() throws Exception {
            ProductBuyRequestDto request = new ProductBuyRequestDto(
                    "req-no-product",
                    List.of(new ProductBuyRequestDto.ProductInfo(productId + 1, 1L))
            );

            // ProductService.buy 의 findById().orElseThrow() 가 NoSuchElementException 을 던진다.
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }

        @Test
        @DisplayName("[현재 동작] requestId가 null이면 락 획득 단계에서 500이 된다")
        void nullRequestId() throws Exception {
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest(null, 1L))))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("[현재 동작] 본문이 비어 있으면 400이 아니라 500이 된다")
        void emptyBody() throws Exception {
            // HttpMessageNotReadableException(원래 400)까지 GlobalExceptionHandler 의 Exception 핸들러가 500으로 덮는다.
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    @DisplayName("POST /v1/products/buy/cancel")
    class Cancel {

        @Test
        @DisplayName("구매 이력이 없으면 200과 총액 0을 반환한다")
        void noBuyHistory() throws Exception {
            mockMvc.perform(post("/v1/products/buy/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ProductBuyCancelRequestDto("req-none"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(0));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("구매 후 취소하면 총액을 반환하고 재고가 원복된다")
        void cancelAfterBuy() throws Exception {
            String requestId = "req-cancel";
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest(requestId, INITIAL_QUANTITY))))
                    .andExpect(status().isOk());
            assertThat(currentQuantity()).isZero();

            mockMvc.perform(post("/v1/products/buy/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ProductBuyCancelRequestDto(requestId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE * INITIAL_QUANTITY));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("동일 requestId로 두 번 취소해도 재고는 한 번만 원복된다")
        void cancelTwiceIsIdempotent() throws Exception {
            String requestId = "req-cancel-twice";
            mockMvc.perform(post("/v1/products/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(buyRequest(requestId, 10L))))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/v1/products/buy/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ProductBuyCancelRequestDto(requestId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE * 10));
            mockMvc.perform(post("/v1/products/buy/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ProductBuyCancelRequestDto(requestId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(PRICE * 10));

            assertThat(currentQuantity()).isEqualTo(INITIAL_QUANTITY);
            assertThat(productTransactionHistoryRepository
                    .findAllByRequestIdAndTransactionType(requestId, ProductTransactionHistory.TransactionType.CANCEL))
                    .hasSize(1);
        }

        @Test
        @DisplayName("이미 락이 잡혀 있으면 500을 반환한다")
        void lockAlreadyHeld() throws Exception {
            String requestId = "req-cancel-locked";
            String lockKey = "cybermonday:product:" + requestId;
            stringRedisTemplate.opsForValue().set(lockKey, requestId);
            try {
                mockMvc.perform(post("/v1/products/buy/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new ProductBuyCancelRequestDto(requestId))))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
            } finally {
                stringRedisTemplate.delete(lockKey);
            }
        }

        @Test
        @DisplayName("[현재 동작] requestId가 null이면 락 획득 단계에서 500이 된다")
        void nullRequestId() throws Exception {
            mockMvc.perform(post("/v1/products/buy/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ProductBuyCancelRequestDto(null))))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }
    }
}
