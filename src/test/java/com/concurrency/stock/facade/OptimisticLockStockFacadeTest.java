package com.concurrency.stock.facade;

import com.concurrency.stock.entity.Stock;
import com.concurrency.stock.repository.StockRepository;
import com.concurrency.stock.service.PessimisticLockService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class OptimisticLockStockFacadeTest {

    @Autowired
    OptimisticLockStockFacade stockService;

    @Autowired
    StockRepository stockRepository;

    @BeforeEach
    void dataInit() {
        Stock stock = new Stock(1L, 100L);
        stockRepository.saveAndFlush(stock);
    }

    @AfterAll
    void delete() {
        stockRepository.deleteAll();
    }

    @Test
    void 동시에_100명이_주문_Pessimistic() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println(System.currentTimeMillis() - startTime); // Optimistic의 경우 Version이 안맞을경우 Lock이 걸리기에 Select를 다시 재시도 하므로,
                                                                    // 읽기 > 쓰기 가 많은 경우 사용하는것이 좋다
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }
}