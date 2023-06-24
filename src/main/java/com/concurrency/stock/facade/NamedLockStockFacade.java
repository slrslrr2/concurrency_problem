package com.concurrency.stock.facade;

import com.concurrency.stock.repository.LockRepository;
import com.concurrency.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


// 실무에서는 NamedLock의 경우 JDBC를 따로 만들어 놓아야한다.
// Table, Row에 Lock을 거는게 아니라 이름으로 다른곳에 별도로 건다? // TODO: 별도 어느 공간에 거는것일까
// 같은 DataSource를 사용하게되면 Connection Pool 이 모자르게 될 경우가 크다 ** 별도의 JDBC를 만들어야한다 TODO://
// NamedLock 은 분산 Lock으로 사용하며, Pessimistic은 타임아웃을 구현하기 힘들다? Named는 손쉽게 구현할 수 있다.
// 이후 정합성을 맞춰야할 경우사용하는데 Transasction의 lock 해제를 주의해야하고, 실제 사용시 복잡하다.
@Component
@RequiredArgsConstructor
public class NamedLockStockFacade {
    private final LockRepository lockRepository;
    private final StockService stockService;

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());

            // 부모의 Transaction과 따로 @Transacation을 걸기위하여
            // @Transactional(propagation = Propagation.REQUIRES_NEW) 선언해줌
            stockService.decrease(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }

}
