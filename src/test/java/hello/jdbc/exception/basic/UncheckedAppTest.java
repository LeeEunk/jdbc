package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
* 런타임 예외는 문서롸를 잘해야 한다.
* */
@Slf4j
public class UncheckedAppTest {
    @Test
    void unchecekd(){
        Controller controller = new Controller();
        assertThatThrownBy(()-> controller.request())
                .isInstanceOf(Exception.class);
    }

    /*
    * 예외 전환할 때는 기존 예외를 반드시! 포함해야 한다.
    * 그렇지 않으면 스택 트레이스를 확인할 때 심각한 문제가 발생한다.
    * */
    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            // System.out에 스택 트레이스를 출력하기 위함.
            // e.printStackTrace();
            // 실무에선 반드시 log를 사용해야 함
            log.info("ex",e); //로그 출력할 때, 예외만 파라미터에 전달하면 스택 트레이스에 로그를 출력할 수 있다.
        }
    }

    static class Controller {
        Service service = new Service();
        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }
    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }

    }
    static class Repository {
        public void call()  {
            try {
                runSQL();
            } catch (SQLException e) { //Caused by: java.sql.SQLException: ex 기존 예를 포함하면 원인(root cause)을 찾을 수 있음
                throw new RuntimeSQLException(e); //SQLException 이전 예외까지 포함해서 가져올 수 있음 ,기존 예외
            }
        }

        private void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException() {
        }
        
        // 기존 예외 등록
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }

    }
}
