package com.concurrency.stock.service;

import com.concurrency.stock.entity.Stock;
import com.concurrency.stock.repository.StockRepository;
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
class PessimisticLockServiceTest {

    @Autowired
    PessimisticLockService stockService;

    @Autowired
    StockRepository stockRepository;

    @BeforeEach
    void dataInit() {
        Stock stock = new Stock(1L, 100L);
        Stock stock2 = new Stock(2L, 100L);
        stockRepository.saveAndFlush(stock);
        stockRepository.saveAndFlush(stock2);
    }

    @AfterAll
    void delete() {
        stockRepository.deleteAll();
    }

    @Test
    void 동시에_100명이_주문() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println(System.currentTimeMillis() - startTime);

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }
}