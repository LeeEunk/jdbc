package hello.jdbc.connection;

//객체 생성되면 안되서 abstract로 막아둠
public abstract class ConnectionConst {
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}
