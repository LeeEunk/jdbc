package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
* JDBC - DataSource 사용, JdbcUtils 사용
* */

@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource; // 커넥션 획득하는 방법을 추상화함

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?,?)";

        Connection con = null;
        PreparedStatement pstmt = null; // 파라미터를 바인딩할 수 있음, SQL Injection 공격을 예방하려면 반드시 PreparedStatement를 사용해야 함


        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); //int를 반환하는데 영향받은 DB row 수를 의미한다. 여기선 하나의 row만 등록했으므로 1을 반환한다
            return member;
        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
        } finally {
            // 커넥션 부족으로 장애가 발생할 수 있으므로 close해야 함
            close(con, pstmt, null); //반드시 리소스 정리는 해줘야 함. 커넥션이 끊어지지 않고 유지되면 리소스 누수가 생길 수 있음.
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?"; //조회 결과가 항상 1건이므로 while 대신에 if를 사용한다. PK인 member_id 를 항상 지정

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null; // select query 결과를 가지고 있는 통

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); // 조회는 executeQuery() 사용
            
            if(rs.next()){ //rs 내부에 있는 커서를 이동해서 다음 데이터를 조회 가능, 결과가 true면 커서 이동 데이터가 있다는 뜻 , 여러개면 while문 사용
                Member member = new Member();
                member.setMemberId(rs.getString("member_id")); // 커서가 가리키고 있는 위치 String으로 반환
                member.setMoney(rs.getInt("money")); // int 타입으로 반환
                return member;
            }else {
                throw new NoSuchElementException("member not found memberId=" +memberId);
            }
        } catch (SQLException e) {
            log.error("db error",e);
            throw  e;
        }finally {
            close(con, pstmt, rs);
        }

    }

    // 커넥션 유지가 필요하므로 파라미터로 넘어온 커넥션을 사용해야 한다. `con = getConnection()`이 있으면 안된다
    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?"; //조회 결과가 항상 1건이므로 while 대신에 if를 사용한다. PK인 member_id 를 항상 지정

        PreparedStatement pstmt = null;
        ResultSet rs = null; // select query 결과를 가지고 있는 통

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); // 조회는 executeQuery() 사용

            if(rs.next()){ //rs 내부에 있는 커서를 이동해서 다음 데이터를 조회 가능, 결과가 true면 커서 이동 데이터가 있다는 뜻 , 여러개면 while문 사용
                Member member = new Member();
                member.setMemberId(rs.getString("member_id")); // 커서가 가리키고 있는 위치 String으로 반환
                member.setMoney(rs.getInt("money")); // int 타입으로 반환
                return member;
            }else {
                throw new NoSuchElementException("member not found memberId=" +memberId);
            }
        } catch (SQLException e) {
            log.error("db error",e);
            throw  e;
        }finally {
            // connection은 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(con);
        }

    }
    public void update (String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize); // 0 or 1 으로 나와야 함, PK 지정했으므로
        } catch (SQLException e) {
            log.error("db error",e);
            throw  e;
        }finally {
            close(con, pstmt, null);
        }
    }


    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error",e);
            throw  e;
        }finally {
            close(con, pstmt, null);
        }
    }

    // 커넥션 유지가 필요하므로 파라미터로 넘어온 커넥션을 사용해야 한다. `con = getConnection()`이 있으면 안된다
    public void update (Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize); // 0 or 1 으로 나와야 함, PK 지정했으므로
        } catch (SQLException e) {
            log.error("db error",e);
            throw  e;
        }finally {
            // connection은 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }
    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get Connection={}, class={}", con, con.getClass());
        return con;
    }
}
