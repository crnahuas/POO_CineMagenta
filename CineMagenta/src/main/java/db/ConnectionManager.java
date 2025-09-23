package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    // Si estás en local, esto suele bastar:
    private static final String URL =
        "jdbc:mysql://localhost:3306/Cine_DB"
      + "?useSSL=false"
      + "&allowPublicKeyRetrieval=true"
      + "&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASS = "fgvcdrt1!";

    private ConnectionManager() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
