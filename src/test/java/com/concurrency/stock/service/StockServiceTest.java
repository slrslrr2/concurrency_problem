package com.concurrency.stock.service;

import com.concurrency.stock.entity.Stock;
import com.concurrency.stock.repository.StockRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class StockServiceTest {

    @Autowired StockService stockService;
    @Autowired StockRepository stockRepository;

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
    void test_동시에_100명이_주문() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.test_decrease(1L, 1L);
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

    @Test
    void test_동시에_100명이_주문_CompletableFuture(){
        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 100; i++) {
                stockService.test_decrease(2L, 1L);
            }
        }, executorService);

        completableFuture.thenRun(() -> {
            Stock findStock = stockRepository.findById(2L).orElseThrow();
            assertEquals(0L, findStock.getQuantity());
            System.out.println("작업이 완료되었습니다 => " + findStock);
        });

        completableFuture.join();
        System.out.println(System.currentTimeMillis() - startTime);
    }


}