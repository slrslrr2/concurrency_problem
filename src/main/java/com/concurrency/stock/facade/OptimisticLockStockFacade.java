package com.concurrency.stock.facade;

import com.concurrency.stock.service.OptimisticLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptimisticLockStockFacade {
    private final OptimisticLockService optimisticLockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true){ // 실패했을때 재시도
            try {
                optimisticLockService.decrease(id, quantity); break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
