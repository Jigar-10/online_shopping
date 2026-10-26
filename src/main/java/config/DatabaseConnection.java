package config;

import exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    public enum Status {
        CONNECTED("Connected to MySQL"),
        ACCESS_DENIED("Access Denied (Invalid username/password)"),
        DATABASE_NOT_FOUND("Database Not Found"),
        SERVER_UNAVAILABLE("MySQL Server Unavailable"),
        CONNECTION_FAILED("Connection Failed");

        private final String displayText;
        Status(String displayText) { this.displayText = displayText; }
        public String getDisplayText() { return displayText; }
    }

    public record ConnectionResult(Status status, String message, String serverVersion, long responseTimeMs) {
        public boolean isSuccess() {
            return status == Status.CONNECTED;
        }
    }

    public interface StatusListener {
        void onStatusChanged(ConnectionResult result);
    }

    private static final List<StatusListener> listeners = new ArrayList<>();
    private static ConnectionResult lastResult = new ConnectionResult(Status.CONNECTION_FAILED, "Not connected yet", null, 0);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC Driver not found in classpath!", e);
        }
    }

    public static synchronized void addStatusListener(StatusListener listener) {
        listeners.add(listener);
        listener.onStatusChanged(lastResult);
    }

    public static synchronized void removeStatusListener(StatusListener listener) {
        listeners.remove(listener);
    }

    private static synchronized void notifyListeners(ConnectionResult result) {
        lastResult = result;
        for (StatusListener listener : listeners) {
            try {
                listener.onStatusChanged(result);
            } catch (Exception e) {
                logger.error("Error in status listener", e);
            }
        }
    }

    public static ConnectionResult getLastResult() {
        return lastResult;
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig config = DatabaseConfig.getInstance();
        return getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
    }

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Test connection with current active configuration and notify listeners.
     */
    public static ConnectionResult testConnection() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        ConnectionResult res = testConnection(config.getHost(), config.getPort(), config.getDatabase(), config.getUsername(), config.getPassword());
        notifyListeners(res);
        return res;
    }

    /**
     * Test connection with explicit credentials.
     */
    public static ConnectionResult testConnection(String host, int port, String database, String username, String password) {
        long start = System.currentTimeMillis();
        String testUrl = "jdbc:mysql://" + host + ":" + port + "/" + (database != null ? database : "") +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=4000";

        try (Connection conn = DriverManager.getConnection(testUrl, username, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            String serverVersion = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
            long duration = System.currentTimeMillis() - start;
            String msg = "Connected to " + serverVersion + " on " + host + ":" + port + "/" + database;
            logger.info(msg);
            return new ConnectionResult(Status.CONNECTED, msg, serverVersion, duration);
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - start;
            int errorCode = e.getErrorCode();
            String sqlState = e.getSQLState();
            String message = e.getMessage();

            logger.warn("Connection test failed (ErrorCode={}, SQLState={}): {}", errorCode, sqlState, message);

            if (errorCode == 1045 || "28000".equals(sqlState)) {
                return new ConnectionResult(Status.ACCESS_DENIED, "Access denied for user '" + username + "'. Check username/password.", null, duration);
            } else if (errorCode == 1049 || "42000".equals(sqlState)) {
                return new ConnectionResult(Status.DATABASE_NOT_FOUND, "Database '" + database + "' does not exist on server.", null, duration);
            } else if (errorCode == 0 || "08S01".equals(sqlState) || "08001".equals(sqlState)) {
                return new ConnectionResult(Status.SERVER_UNAVAILABLE, "MySQL server unavailable on " + host + ":" + port + ". Ensure MySQL service is running.", null, duration);
            } else {
                return new ConnectionResult(Status.CONNECTION_FAILED, "Connection failed: " + message, null, duration);
            }
        }
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    logger.debug("Error closing resource: {}", e.getMessage());
                }
            }
        }
    }

    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("Failed to rollback transaction", e);
            }
        }
    }
}
