package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    // 트랜잭션을 시작하려면 커넥션이 필요
    // 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 함 = 같은 세션 사용하기 위해
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 시작
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money); // fromId 회원의 돈을 money만큼 감소 -> updateSQL 실행
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money); // toId 회원의 돈을 money만큼 증가 -> updateSQL 실행
        // 커밋, 롤백
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
