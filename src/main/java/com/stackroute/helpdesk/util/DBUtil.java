package com.stackroute.helpdesk.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtil {
    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());
    private static final Properties props = new Properties();
    private static String driver = "oracle.jdbc.OracleDriver";
    private static String url = "jdbc:oracle:thin:@localhost:1521:FREE";
    private static String user = "C##itcuser";
    private static String password = "itcuser";

    static {
        loadConfig();
        initializeDatabase();
    }

    public static synchronized void loadConfig() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            InputStream stream = is != null ? is : DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (stream != null) {
                props.load(stream);
                driver = props.getProperty("oracle.driver", driver);
                url = props.getProperty("oracle.url", url);
                user = props.getProperty("oracle.user", user);
                password = props.getProperty("oracle.password", password);
            }
            if (driver != null && !driver.isEmpty()) {
                Class.forName(driver);
            }
            System.out.println("=================================================================");
            System.out.println(">>> [DBUtil] ACTIVE DATABASE: ORACLE");
            System.out.println(">>> [DBUtil] JDBC URL:       " + url);
            System.out.println(">>> [DBUtil] USER:           " + user);
            System.out.println("=================================================================");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load database configuration", e);
        }
    }

    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            // Automatically add security_question and security_answer to USERS table if not present
            try {
                stmt.execute("ALTER TABLE users ADD (security_question VARCHAR2(255), security_answer VARCHAR2(255))");
                System.out.println(">>> [DBUtil] Migrated USERS table: added security_question and security_answer");
            } catch (SQLException ignored) {
                // Column already exists (ORA-01430)
            }
            // Seed default security question and answer for users with empty values
            try {
                stmt.execute("UPDATE users SET security_question = 'What was the name of your first pet?', security_answer = 'fluffy' WHERE security_question IS NULL OR security_answer IS NULL");
                stmt.execute("UPDATE users SET password = 'user123' WHERE LOWER(email) = 'john@example.com'");
            } catch (SQLException ignored) {}
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database initialization note: " + e.getMessage());
        } finally {
            close(conn, stmt);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
}