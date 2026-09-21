package topicmanagement.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class DatabaseConfigLoaderTest {

  @Test
  void load_readsFromSystemProperties() {
    Properties props = new Properties();
    props.setProperty("db.url", "jdbc:mysql://localhost:3306/test_db");
    props.setProperty("db.username", "testuser");
    props.setProperty("db.password", "testpass");

    DatabaseConfigLoader.DatabaseSettings settings = DatabaseConfigLoader.load(props, Map.of());
    assertEquals("jdbc:mysql://localhost:3306/test_db", settings.url());
    assertEquals("testuser", settings.username());
    assertEquals("testpass", settings.password());
    assertTrue(settings.sourceDescription().contains("JVM system properties"));
  }

  @Test
  void load_readsFromEnvironmentVariables() {
    Properties props = new Properties();
    Map<String, String> env = new HashMap<>();
    env.put("DB_URL", "jdbc:mysql://localhost:3306/env_db");
    env.put("DB_USERNAME", "envuser");
    env.put("DB_PASSWORD", "envpass");

    DatabaseConfigLoader.DatabaseSettings settings = DatabaseConfigLoader.load(props, env);
    assertEquals("jdbc:mysql://localhost:3306/env_db", settings.url());
    assertEquals("envuser", settings.username());
    assertEquals("envpass", settings.password());
    assertTrue(settings.sourceDescription().contains("environment variables"));
  }

  @Test
  void load_throwsExceptionWhenMissingProperties() {
    Properties props = new Properties();
    assertThrows(IllegalStateException.class, () -> DatabaseConfigLoader.load(props, Map.of()));
  }
}
