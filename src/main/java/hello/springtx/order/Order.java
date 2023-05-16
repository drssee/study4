package hello.springtx.order;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "orders") //orders라는 테이블과 매핑, order가 예약어라서
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String userName; //정상, 예외, 잔고부족
    private String payStatus; //대기, 완료
}
