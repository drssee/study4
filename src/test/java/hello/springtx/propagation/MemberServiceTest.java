package hello.springtx.propagation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 대원칙
 * 같은 물리 트랜잭션 내의 논리 트랜잭션들이 모두 논리커밋 되어야 물리커밋이 된다
 * 신규 트랜잭션이 물리 트랜잭션이다(물리 커넥션을 가진다)
 */
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;

    /**
     * memberService @Transactional:OFF
     * memberRepository @Transactional:ON
     * logRepository @Transactional:ON
     */
    @Test
    void outerTxOff_success() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transactional:OFF
     * memberRepository @Transactional:ON
     * logRepository @Transactional:ON Exception
     */
    @Test
    void outerTxOff_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        Assertions.assertThrows(RuntimeException.class, () -> {
            memberService.joinV1(username);
        });

        //then
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService @Transactional:ON
     */
    @Test
    void singleTx() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transactional:ON
     * memberRepository @Transactional:ON
     * logRepository @Transactional:ON
     */
    @Test
    void outerTxOn_Success() {
        //given
        String username = "outerTxOn_Success";

        //when
        Assertions.assertThrows(RuntimeException.class, () -> {
            memberService.joinV1(username);
        });

        //then
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService @Transactional:ON
     * memberRepository @Transactional:ON
     * logRepository @Transactional:ON Exception
     */
    //기본전파로 존재하는 트랜잭션에 참여해도
    //그 트랜잭션의 aop코드는 존재함 단지 물리 커넥션을 가진게 아니라(신규 트랜잭션x),
    //상위 트랜잭션에 결과를 마킹만함
    @Test
    void outerTxOn_fail() {
        //given
        String username = "로그예외_outerTxOn_fail";

        //when
        Assertions.assertThrows(RuntimeException.class, () -> {
            memberService.joinV1(username);
        });

        //then
        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService @Transactional:ON
     * memberRepository @Transactional:ON
     * logRepository @Transactional:ON Exception
     */
    @Test
    void recoverException_fail() {
        /*
        참여한 내부 트랜잭션에서 런타임예외가 발생해 롤백 -> 하지만 내부 참여 트랜잭션이라 롤백only만 마크함
        던져진 예외가 서비스까지 올라간다 -> 하지만 서비스예서 예외를 잡아서 처리 후 정상적으로 aop에게 흐름이 넘어감
        aop는 외부 트랜잭션이 정상종료 되었으니 커밋을 요청함 -> 하지만 이미 트랜잭션 롤백only가 마크되어 롤백됨
        그리고 외부 트랜잭션 입장에서는 커밋을 요청했지만 롤백이 발생한 경우니 기대하지않은롤백 예외가 터진다
         */

        //given
        String username = "로그예외_recoverException_fail";

        //when
        memberService.joinV2(username);

        //then
        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService @Transactional:ON
     * memberRepository @Transactional:ON
     * logRepository @Transactional:ON(REQUIRES_NEW) Exception
     */
    @Test
    void recoverException_success() {
        /*
        참여한 내부 트랜잭션에서 런타임예외가 발생해 롤백 -> 하지만 내부 참여 트랜잭션이라 롤백only만 마크함
        던져진 예외가 서비스까지 올라간다 -> 하지만 서비스예서 예외를 잡아서 처리 후 정상적으로 aop에게 흐름이 넘어감
        aop는 외부 트랜잭션이 정상종료 되었으니 커밋을 요청함 -> 하지만 이미 트랜잭션 롤백only가 마크되어 롤백됨
        그리고 외부 트랜잭션 입장에서는 커밋을 요청했지만 롤백이 발생한 경우니 기대하지않은롤백 예외가 터진다
         */

        //given
        String username = "로그예외_recoverException_success";

        //when
        memberService.joinV2(username);

        //then
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }
}