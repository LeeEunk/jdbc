package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;


/*
* 인터페이스 목적은 구현체를 쉽게 변경하기 위함
* */
public interface MemberRepositoryEx {
    Member save(Member member) throws SQLException;
    Member findById(String memberId) throws SQLException;
    void update(String memberId, int money) throws SQLException;
    void delete(String memberId) throws SQLException;
}
