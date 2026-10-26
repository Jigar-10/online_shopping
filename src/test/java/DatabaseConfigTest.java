import config.DatabaseConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    @DisplayName("Should generate valid JDBC URL with parameters")
    void testJdbcUrlGeneration() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        config.setHost("localhost");
        config.setPort(3306);
        config.setDatabase("online_shopping");

        String url = config.getJdbcUrl();
        assertTrue(url.startsWith("jdbc:mysql://localhost:3306/online_shopping"));
        assertTrue(url.contains("useSSL=false"));
        assertTrue(url.contains("allowPublicKeyRetrieval=true"));
    }

    @Test
    @DisplayName("Should mask password in toString representation")
    void testPasswordMasking() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        config.setPassword("SuperSecret123!");

        String str = config.toString();
        assertFalse(str.contains("SuperSecret123!"));
        assertTrue(str.contains("password='***'"));
    }
}
