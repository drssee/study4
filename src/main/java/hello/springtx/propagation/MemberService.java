package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username) {
        Member member = new Member(username);
        Log log1 = new Log(username);

        log.info("= memberRepository 호출 시작 =");
        memberRepository.save(member);
        log.info("= memberRepository 호출 종료 =");

        log.info("= logRepository 호출 시작 =");
        logRepository.save(log1);
        log.info("= logRepository 호출 종료 =");
    }

    @Transactional
    public void joinV2(String username) {
        Member member = new Member(username);
        Log log1 = new Log(username);

        log.info("= memberRepository 호출 시작 =");
        memberRepository.save(member);
        log.info("= memberRepository 호출 종료 =");

        log.info("= logRepository 호출 시작 =");
        try {
            logRepository.save(log1);
        } catch (RuntimeException e) {
            log.info("log 저장에 실패했습니다. log1Message={}", log1.getMessage());
            log.info("정상 흐름 반환");
        }
        log.info("= logRepository 호출 종료 =");
    }
}
