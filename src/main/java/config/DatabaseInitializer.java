package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public record DbStatus(boolean databaseExists, List<String> tables, List<String> views) {}

    public static DbStatus inspectDatabase() {
        List<String> tables = new ArrayList<>();
        List<String> views = new ArrayList<>();
        boolean exists = false;

        DatabaseConfig config = DatabaseConfig.getInstance();
        String dbName = config.getDatabase();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            exists = true;

            // Retrieve tables
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT TABLE_NAME, TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + dbName + "'")) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    String type = rs.getString("TABLE_TYPE");
                    if ("VIEW".equalsIgnoreCase(type)) {
                        views.add(name);
                    } else {
                        tables.add(name);
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not inspect database: {}", e.getMessage());
        }

        return new DbStatus(exists, tables, views);
    }

    public static int executeSqlScript(File scriptFile) throws Exception {
        if (!scriptFile.exists()) {
            throw new IllegalArgumentException("SQL file not found: " + scriptFile.getAbsolutePath());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            return runScript(reader);
        }
    }

    public static int executeSqlScript(InputStream in) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return runScript(reader);
        }
    }

    private static int runScript(BufferedReader reader) throws Exception {
        int executedCount = 0;
        DatabaseConfig config = DatabaseConfig.getInstance();

        // First connect to MySQL without database in case database needs to be created
        String serverUrl = config.getJdbcUrl("");
        try (Connection conn = DatabaseConnection.getConnection(serverUrl, config.getUsername(), config.getPassword());
             Statement stmt = conn.createStatement()) {

            String delimiter = ";";
            StringBuilder currentStatement = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("#")) {
                    continue;
                }

                if (trimmed.toUpperCase().startsWith("DELIMITER")) {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length > 1) {
                        delimiter = parts[1];
                    }
                    continue;
                }

                currentStatement.append(line).append("\n");

                if (trimmed.endsWith(delimiter)) {
                    String sql = currentStatement.toString().trim();
                    if (sql.endsWith(delimiter)) {
                        sql = sql.substring(0, sql.length() - delimiter.length()).trim();
                    }

                    if (!sql.isEmpty()) {
                        // Skip CREATE USER or GRANT commands that may fail without root admin
                        String upper = sql.toUpperCase();
                        if (upper.startsWith("CREATE USER") || upper.startsWith("GRANT")) {
                            logger.info("Skipping administrative statement during app setup: {}", sql);
                            currentStatement.setLength(0);
                            continue;
                        }

                        try {
                            stmt.execute(sql);
                            executedCount++;
                        } catch (SQLException e) {
                            logger.warn("Error executing statement ({}): {}", sql.length() > 60 ? sql.substring(0, 60) + "..." : sql, e.getMessage());
                        }
                    }
                    currentStatement.setLength(0);
                }
            }
        }

        logger.info("SQL script execution finished. Executed {} statements.", executedCount);
        return executedCount;
    }
}
