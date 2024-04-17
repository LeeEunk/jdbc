package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/*
* 트랜잭션 - 트랜잭션 템플릿
* */
@Slf4j
public class MemberServiceV3_2 {

//    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    // 트랜잭션 = 수동 커밋 모드
    // 트랜잭션을 시작하려면 커넥션이 필요
    // 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 함 = 같은 세션 사용하기 위해
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        txTemplate.executeWithoutResult((status) -> {
            // 비즈니스 로직
            try {
                bizLogic(fromId, toId, money); // 호출하면 `SQLException` 체크 예외를 넘겨줌
            } catch (SQLException e) {
                throw new IllegalStateException(e); // 해당 람다에서 체크 예외를 밖으로 던질 수 없기 때문에 언체크 예외로 바꾸어 던지게 함
            }
        });
    }

    private void bizLogic(String fromId, String toId, int money ) throws SQLException {
        // 비즈니스 로직
        // 시작
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money); // fromId 회원의 돈을 money만큼 감소 -> updateSQL 실행
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money); // toId 회원의 돈을 money만큼 증가 -> updateSQL 실행
    }


    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
