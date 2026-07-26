package brucehan.product.application;

import brucehan.product.domain.entity.Stock;
import brucehan.product.domain.repository.StockJpaRepository;
import brucehan.product.presentation.request.StockRequestDto;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class StockServiceTest {
    @Autowired
    StockService stockService;
    @Autowired
    StockJpaRepository stockJpaRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    void before() {
        stockJpaRepository.save(new Stock(1000, 1L));
    }

    @AfterEach
    void after() {
        stockJpaRepository.deleteAll();
    }

    @Test
    void testConcurrency() throws InterruptedException {
        StockRequestDto requestDto = new StockRequestDto(1L, 1, 1L);
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseStockAndPublish(requestDto);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executorService.shutdown();

        Integer quantity = stockJpaRepository.findQuantityByProductId(1L).get();
        // 예상 : 100 - (1 * 100) = 0
        assertThat(quantity).isEqualTo(0);
    }
}