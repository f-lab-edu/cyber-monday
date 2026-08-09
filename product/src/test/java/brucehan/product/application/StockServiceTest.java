package brucehan.product.application;

import brucehan.product.config.exception.BusinessException;
import brucehan.product.domain.entity.Stock;
import brucehan.product.domain.repository.StockJpaRepository;
import brucehan.product.presentation.request.StockRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class StockServiceTest {
    @Autowired
    StockService stockService;
    @Autowired
    StockJpaRepository stockJpaRepository;

    @BeforeEach
    void before() {
        stockJpaRepository.deleteAll();
    }

    @Test
    void testConcurrency() throws InterruptedException {
        stockJpaRepository.save(new Stock(100, 1L));
        StockRequestDto requestDto = new StockRequestDto(1L, 1, 1L);
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger purchased = new AtomicInteger(0);
        AtomicInteger soldOut = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseStock(requestDto);
                    purchased.getAndIncrement();
                } catch (BusinessException e) {
                    soldOut.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executorService.shutdown();

        Stock stock = stockJpaRepository.findByProductId(1L).get();
        // 예상 : 100개 - (1000번주문 * 1개씩주문) = 0개, 매진은 900건
        assertThat(stock.getQuantity()).isEqualTo(0);
        assertThat(purchased.get()).isEqualTo(100);
        assertThat(soldOut.get()).isEqualTo(900);
        log.info("purchase count : {}", purchased.get());
        log.info("soldOut count : {}", soldOut.get());
    }

    @Test
    void testFailPurchaseRemaining() throws InterruptedException {
        stockJpaRepository.save(new Stock(3, 1L));
        StockRequestDto requestDto = new StockRequestDto(1L, 5, 1L);
        int threadCount = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger purchased = new AtomicInteger(0);
        AtomicInteger soldOut = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseStock(requestDto);
                    purchased.getAndIncrement();
                } catch (BusinessException e) {
                    soldOut.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executorService.shutdown();

        Stock stock = stockJpaRepository.findByProductId(1L).get();
        // 예상 : 3개 - (1번주문 * 5개씩주문) => 3개 남음
        assertThat(stock.getQuantity()).isEqualTo(3);
        assertThat(purchased.get()).isEqualTo(0);
        assertThat(soldOut.get()).isEqualTo(1);
        log.info("purchase count : {}", purchased.get());
        log.info("soldOut count : {}", soldOut.get());
    }

    @Test
    void testPortionPurchaseRemaining() throws InterruptedException {
        stockJpaRepository.save(new Stock(10, 1L));
        StockRequestDto requestDto = new StockRequestDto(1L, 3, 1L);
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger purchased = new AtomicInteger(0);
        AtomicInteger soldOut = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseStock(requestDto);
                    purchased.getAndIncrement();
                } catch (BusinessException e) {
                    soldOut.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executorService.shutdown();

        Stock stock = stockJpaRepository.findByProductId(1L).get();
        // 예상 : 10개 - (5번주문 * 3개씩주문) => 1개 남음, 구매 3번, 매진 2번
        assertThat(stock.getQuantity()).isEqualTo(1);
        assertThat(purchased.get()).isEqualTo(3);
        assertThat(soldOut.get()).isEqualTo(2);
        log.info("purchase count : {}", purchased.get());
        log.info("soldOut count : {}", soldOut.get());
    }


    @Test
    void testPortionPurchaseRemaining_boundary() throws InterruptedException {
        stockJpaRepository.save(new Stock(100, 1L));
        StockRequestDto requestDto = new StockRequestDto(1L, 100, 1L);
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger purchased = new AtomicInteger(0);
        AtomicInteger soldOut = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseStock(requestDto);
                    purchased.getAndIncrement();
                } catch (BusinessException e) {
                    soldOut.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executorService.shutdown();

        Stock stock = stockJpaRepository.findByProductId(1L).get();
        // 예상 : 100개 - (10번주문 * 100개씩주문) = 0개, 1건 결제, 매진은 9건
        assertThat(stock.getQuantity()).isEqualTo(0);
        assertThat(purchased.get()).isEqualTo(1);
        assertThat(soldOut.get()).isEqualTo(9);
        log.info("purchase count : {}", purchased.get());
        log.info("soldOut count : {}", soldOut.get());
    }
}