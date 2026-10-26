package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILENAME = "db.properties";
    private static final DatabaseConfig INSTANCE = new DatabaseConfig();

    private String host = "localhost";
    private int port = 3306;
    private String database = "online_shopping";
    private String username = "root";
    private String password = "";
    private boolean useSSL = false;
    private boolean allowPublicKeyRetrieval = true;
    private String serverTimezone = "UTC";
    private int connectionTimeoutMs = 5000;

    private DatabaseConfig() {
        load();
    }

    public static DatabaseConfig getInstance() {
        return INSTANCE;
    }

    public synchronized void load() {
        Properties props = new Properties();
        File externalFile = new File(CONFIG_FILENAME);

        boolean loaded = false;
        if (externalFile.exists()) {
            try (InputStream in = new FileInputStream(externalFile)) {
                props.load(in);
                logger.info("Loaded database configuration from {}", externalFile.getAbsolutePath());
                loaded = true;
            } catch (Exception e) {
                logger.warn("Could not read external db.properties: {}", e.getMessage());
            }
        }

        if (!loaded) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILENAME)) {
                if (in != null) {
                    props.load(in);
                    logger.info("Loaded database configuration from classpath {}", CONFIG_FILENAME);
                    loaded = true;
                }
            } catch (Exception e) {
                logger.warn("Could not read classpath db.properties: {}", e.getMessage());
            }
        }

        if (loaded) {
            this.host = props.getProperty("db.host", this.host);
            try {
                this.port = Integer.parseInt(props.getProperty("db.port", String.valueOf(this.port)));
            } catch (NumberFormatException ignored) {}
            this.database = props.getProperty("db.name", this.database);
            this.username = props.getProperty("db.username", this.username);
            this.password = props.getProperty("db.password", this.password);
            this.useSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", String.valueOf(this.useSSL)));
            this.allowPublicKeyRetrieval = Boolean.parseBoolean(props.getProperty("db.allowPublicKeyRetrieval", String.valueOf(this.allowPublicKeyRetrieval)));
            this.serverTimezone = props.getProperty("db.serverTimezone", this.serverTimezone);
            try {
                this.connectionTimeoutMs = Integer.parseInt(props.getProperty("db.connectionTimeoutMs", String.valueOf(this.connectionTimeoutMs)));
            } catch (NumberFormatException ignored) {}
        }
    }

    public synchronized void save() {
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.name", database);
        props.setProperty("db.username", username);
        props.setProperty("db.password", password != null ? password : "");
        props.setProperty("db.useSSL", String.valueOf(useSSL));
        props.setProperty("db.allowPublicKeyRetrieval", String.valueOf(allowPublicKeyRetrieval));
        props.setProperty("db.serverTimezone", serverTimezone);
        props.setProperty("db.connectionTimeoutMs", String.valueOf(connectionTimeoutMs));

        try (OutputStream out = new FileOutputStream(CONFIG_FILENAME)) {
            props.store(out, "Online Shopping DBMS Configuration");
            logger.info("Saved database configuration to {}", CONFIG_FILENAME);
        } catch (Exception e) {
            logger.error("Failed to save configuration to {}: {}", CONFIG_FILENAME, e.getMessage());
        }
    }

    public String getJdbcUrl() {
        return getJdbcUrl(this.database);
    }

    public String getJdbcUrl(String dbName) {
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://").append(host).append(":").append(port);
        if (dbName != null && !dbName.isBlank()) {
            sb.append("/").append(dbName);
        }
        sb.append("?useSSL=").append(useSSL)
          .append("&allowPublicKeyRetrieval=").append(allowPublicKeyRetrieval)
          .append("&serverTimezone=").append(serverTimezone)
          .append("&connectTimeout=").append(connectionTimeoutMs);
        return sb.toString();
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isUseSSL() { return useSSL; }
    public void setUseSSL(boolean useSSL) { this.useSSL = useSSL; }

    public boolean isAllowPublicKeyRetrieval() { return allowPublicKeyRetrieval; }
    public void setAllowPublicKeyRetrieval(boolean allowPublicKeyRetrieval) { this.allowPublicKeyRetrieval = allowPublicKeyRetrieval; }

    public String getServerTimezone() { return serverTimezone; }
    public void setServerTimezone(String serverTimezone) { this.serverTimezone = serverTimezone; }

    public int getConnectionTimeoutMs() { return connectionTimeoutMs; }
    public void setConnectionTimeoutMs(int connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", password='***'" +
                '}';
    }
}
