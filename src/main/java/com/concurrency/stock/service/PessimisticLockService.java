package com.concurrency.stock.service;

import com.concurrency.stock.entity.Stock;
import com.concurrency.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessimisticLockService {
    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Long id, Long quantity) {
//        Stock stock = stockRepository.findByIdWithPessimisticLock(id);
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}
