package lms.db;

import java.sql.*;

public class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/lms_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private static Connection conn = null;

    public static Connection get() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
        return conn;
    }

    public static boolean test() {
        try {
            Connection c = get();
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
