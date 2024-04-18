package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;


/*
* JDBCTemplate 사용
*
* */

@Slf4j
public class MemberRepositoryV5 implements MemberRepository{

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    // 저장
    @Override // 인터페이스 사용하면 오버라이드 사용하는게 좋음 -> 컴파일러가 오류를 잡아주기 때문
    public Member save(Member member)  {
        String sql = "insert into member(member_id, money) values (?,?)";
        template.update(sql, member.getMemberId(), member.getMoney());  //executeUpdate();

        return member;
    }

    // 조회
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?"; //조회 결과가 항상 1건이므로 while 대신에 if를 사용한다. PK인 member_id 를 항상 지정

        // 1건 조회
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    // 수정
    @Override
    public void update (String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);


    }


    // 삭제
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql,memberId);

    }

  
    // 커넥션 닫기, 동기화 둘다 필요없음. JDBC Template 다 알아서 해줌
}
