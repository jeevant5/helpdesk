package com.stackroute.helpdesk.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static String dbType = "h2";
    private static String driver = "org.h2.Driver";
    private static String url = "jdbc:h2:mem:helpdeskdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static String user = "sa";
    private static String password = "";
    private static volatile boolean initialized = false;

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            InputStream stream = is != null ? is : DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (stream != null) {
                props.load(stream);
                dbType = props.getProperty("db.type", "h2").trim().toLowerCase();
                driver = props.getProperty(dbType + ".driver", driver);
                url = props.getProperty(dbType + ".url", url);
                user = props.getProperty(dbType + ".user", user);
                password = props.getProperty(dbType + ".password", password);
            }
            if (driver != null && !driver.isEmpty()) {
                Class.forName(driver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void initializeDatabase() {
        if (initialized) return;
        try {
            if ("h2".equalsIgnoreCase(dbType)) {
                try (Connection conn = DriverManager.getConnection(url, user, password)) {
                    runSqlScript(conn, "schema-h2.sql");
                }
            }
            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runSqlScript(Connection conn, String scriptName) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptName);
            if (is == null) {
                is = DBUtil.class.getClassLoader().getResourceAsStream(scriptName);
            }
            if (is == null) {
                System.err.println("SQL Script not found: " + scriptName);
                return;
            }
            StringBuilder fullScript = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("--") || line.isEmpty()) {
                        continue;
                    }
                    fullScript.append(line).append(" ");
                }
            }

            String[] statements = fullScript.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            System.err.println("Error executing SQL [" + sql + "]: " + ex.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized && "h2".equalsIgnoreCase(dbType)) {
            initializeDatabase();
        }
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

    public static String getDbType() {
        return dbType;
    }
}