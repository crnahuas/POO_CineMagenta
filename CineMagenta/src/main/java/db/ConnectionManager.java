package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    private static final String URL  = "jdbc:mysql://127.0.0.1:3306/Cine_DB_S7?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "fgvcdrt1!";

    private ConnectionManager() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static Connection getTx() throws SQLException {
        Connection c = get();
        c.setAutoCommit(false);
        return c;
    }
}
