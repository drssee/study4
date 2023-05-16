package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    //JPA는 트랜잭션 커밋 시점에 Order 데이터를 DB에 반영한다
    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order 호출");
        orderRepository.save(order);

        log.info("결제 프로세스 진입");
        if (order.getUserName().equals("예외")) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        } else if (order.getUserName().equals("잔고부족")) {
            log.info("잔고 부족 비즈니스 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족합니다");
        } else {
            //정상 승인
            //여기에 JPA save 로직을 넣으면, 체크 예외 발생시 영속 컨텍스트에 엔티티가 없어 커밋될때 insert가 되지 않는다
            log.info("정상 승인");
            order.setPayStatus("완료");
        }
    }
}
