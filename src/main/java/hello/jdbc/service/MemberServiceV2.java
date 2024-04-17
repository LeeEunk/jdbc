package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/*
* 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
* */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    // 트랜잭션 = 수동 커밋 모드
    // 트랜잭션을 시작하려면 커넥션이 필요
    // 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 함 = 같은 세션 사용하기 위해
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try{
            con.setAutoCommit(false); //트랜잭션 시작
            bizLogic(con, fromId, toId, money);
            // 커밋, 롤백
            con.commit(); // 성공 시 커밋
        }catch (Exception e){
            con.rollback(); // 실패 시 롤백
            throw new IllegalStateException(e);
        }finally {
            release(con);

        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money ) throws SQLException {
        // 비즈니스 로직
        // 시작
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money); // fromId 회원의 돈을 money만큼 감소 -> updateSQL 실행
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money); // toId 회원의 돈을 money만큼 증가 -> updateSQL 실행
    }

    private void release(Connection con) {
        if(con != null){
            try {
                con.setAutoCommit(true); //커넥션 풀 고려, 커넥션 풀에 반납되기 때문에 기본 값으로 돌려줌
                con.close();
            }catch (Exception e){
                log.info("error", e); // exception는 {} 사용 안해도 됨, 파라미터 있을 때만 ={} 사용
            }
        }
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
