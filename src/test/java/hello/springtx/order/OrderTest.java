package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void Order() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUserName("정상");

        //when
        orderService.order(order);

        //then
        Order findOrder = orderRepository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    @Test
    void runTimeException() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUserName("예외");

        //when
        Assertions.assertThatThrownBy(() -> {
            orderService.order(order);
        }).isInstanceOf(RuntimeException.class);

        //then
        //런타임 exception이라 롤백이 되어 조회가 안되야함
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        Assertions.assertThat(orderOptional.isEmpty()).isTrue();
    }

    @Test
    void bizException() {
        //given
        Order order = new Order();
        order.setUserName("잔고부족");

        //when
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내");
        }

        //then
        //체크드 예외라 커밋되어서, 조회가 되어야함
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        Assertions.assertThat(orderOptional.isPresent()).isTrue();
        Assertions.assertThat(orderOptional.get().getPayStatus()).isEqualTo("대기");
    }
}