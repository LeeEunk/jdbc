package hello.jdbc.exception.translator;

import hello.jdbc.connection.ConnectionConst.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init(){
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammar";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){ //Column "BAD" not found; SQL statement:
            assertThat(e.getErrorCode()).isEqualTo(42122);
            // throw new BadSqlGrammarException(e); -> 직접 예외를 확인 후, 예외로 변환하는 걸 구현하기가 복잡해짐 + 데이터베이스마다 오류코드가 다름
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            log.info("error", e);
        }
    }
    
    /* spring boot 에서 예외 변환기 제공 */
    @Test
    void exceptionTranslator() {
        String sql = "select bad grammar";

        try{
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){
            assertThat(e.getErrorCode()).isEqualTo(42122);

            //org.springframework.jdbc.support.sql-error-codes.xml -> 해당 파일에 각 데이터베이스가 갖고있는 오류ㅜ코드를 모아둠
            SQLErrorCodeSQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);

            //BadSqlGrammarException
            // 첫 번째 파라미터는 읽을 수 있는 설명, 두번째는 실행한 sql, 마지막은 발생된 SQLException을 전달
            DataAccessException resultEx = exTranslator.translate("test", sql, e); // org.springframework.jdbc.BadSqlGrammarException: test; bad SQL grammar [select bad grammer]
            log.info("resultEx", resultEx);
            assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
