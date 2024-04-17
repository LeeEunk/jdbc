package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/*
* 트랜잭션 - 트랜잭션 매니저
* */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    // 트랜잭션 = 수동 커밋 모드
    // 트랜잭션을 시작하려면 커넥션이 필요
    // 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 함 = 같은 세션 사용하기 위해
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            bizLogic(fromId, toId, money);
            // 트랜잭션은 커밋하거나 롤백하면 종료된다.
            // 커밋, 롤백
            transactionManager.commit(status); // 성공 시 커밋
        }catch (Exception e){
            transactionManager.rollback(status); // 실패 시 롤백
            throw new IllegalStateException(e);
        }
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
