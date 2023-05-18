package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

//gettransaction -> tx매니저가 datasource에서 커넥션을 가져와 트랜잭션 처리후(autocommit false) -> 쓰레드로컬저장소에 커넥션을 넣어둠
//이후로직에서 커넥션이 필요한 로직이 있을시, 쓰레드로컬저장소에 커넥션이 있으면 그 커넥션을 가져와 트랜잭션 합류
//없는 경우는 그냥 일반적인 짧은 트랜잭션
//그리고 @Transactional이 붙으면 스프링 AOP에서 타겟의 프록시 객체를 만들고(내부에 타겟 저장) 지금 수동으로 등록하는 트랜잭션 작업을
//스프링 AOP가 자동으로 해줌
@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋");
        txManager.commit(status);

        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollBack() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백");
        txManager.rollback(status);

        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void doubleCommit() {
        log.info("트랜잭션1 시작");
        TransactionStatus status1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(status1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus status2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.commit(status2);
        log.info("트랜잭션2 커밋 완료");
    }

    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus status1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(status1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus status2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백");
        txManager.rollback(status2);
        log.info("트랜잭션2 롤백 완료");
    }

    @Test
    void propagation() {
        log.info("트랜잭션 랩 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        try {
            //내부 트랜잭션을 가진 로직
            doubleTx();
            //내부 트랜잭션을 가진 로직에 오류가 발생해 트랜잭션 랩도 롤백 되어야함
            log.info("트랜잭션 랩 커밋");
            txManager.commit(status);
        } catch (RuntimeException e) {
            log.info("트랜잭션 랩 롤백");
            txManager.rollback(status);
        }
    }

    private void doubleTx() {
        log.info("트랜잭션1 시작");
        TransactionStatus status1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(status1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus status2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 오류(런타임에러)");
        throw new RuntimeException();
    }
}
