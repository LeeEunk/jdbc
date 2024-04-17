package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

/*
* 트랜잭션 - @Transactional AOP
* */
@Slf4j
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional // 메서드에 붙여도 되고 클래스에 붙여도 됨. 클래스에 붙이면 외부에서 호출 가능한 public 메서드가 AOP 대상이 된다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
         // 비즈니스 로직
        bizLogic(fromId, toId, money); // 호출하면 `SQLException` 체크 예외를 넘겨줌

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
