package topicmanagement.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseConfigLoaderTest {
  @TempDir Path temporaryDirectory;

  @Test
  void loadsStandardKeysFromExternalFile() throws IOException {
    Path externalFile = writeDefaultFile("db.url=jdbc:mysql://localhost/test\ndb.username=user\ndb.password=secret\n");

    var settings = DatabaseConfigLoader.load(propertiesFor(), Map.of());

    assertEquals("jdbc:mysql://localhost/test", settings.url());
    assertEquals("user", settings.username());
    assertEquals("secret", settings.password());
    assertEquals(externalFile, settings.externalFile());
    assertEquals("external file", settings.sourceDescription());
  }

  @Test
  void reportsMissingUrlClearly() throws IOException {
    assertMissing("db.username=user\ndb.password=secret\n", "db.url");
  }

  @Test
  void reportsMissingUsernameClearly() throws IOException {
    assertMissing("db.url=jdbc:mysql://localhost/test\ndb.password=secret\n", "db.username");
  }

  @Test
  void reportsMissingPasswordClearly() throws IOException {
    assertMissing("db.url=jdbc:mysql://localhost/test\ndb.username=user\n", "db.password");
  }

  @Test
  void systemPropertiesOverrideEnvironmentAndExternalFile() throws IOException {
    Path externalFile = writeDefaultFile("db.url=external-url\ndb.username=external-user\ndb.password=external-password\n");
    Properties systemProperties = propertiesFor();
    systemProperties.setProperty("db.url", "system-url");
    systemProperties.setProperty("DB_PASSWORD", "system-password");

    var settings =
        DatabaseConfigLoader.load(
            systemProperties,
            Map.of("DB_URL", "environment-url", "DB_USERNAME", "environment-user"));

    assertEquals("system-url", settings.url());
    assertEquals("environment-user", settings.username());
    assertEquals("system-password", settings.password());
    assertEquals("JVM system properties, environment variables", settings.sourceDescription());
  }

  @Test
  void customConfigPathOverridesCatalinaBaseDefault() throws IOException {
    Path customFile = temporaryDirectory.resolve("custom-db.properties");
    Files.writeString(customFile, "db.url=custom-url\ndb.username=custom-user\ndb.password=custom-password\n");
    Properties systemProperties = new Properties();
    systemProperties.setProperty("catalina.base", temporaryDirectory.resolve("unused").toString());
    systemProperties.setProperty("db.config.file", customFile.toString());

    var settings = DatabaseConfigLoader.load(systemProperties, Map.of());

    assertEquals(customFile.toAbsolutePath().normalize(), settings.externalFile());
    assertEquals("custom-url", settings.url());
  }

  private void assertMissing(String content, String missingKey) throws IOException {
    Path externalFile = writeDefaultFile(content);
    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () -> DatabaseConfigLoader.load(propertiesFor(), Map.of()));
    assertTrue(error.getMessage().contains(missingKey));
    assertTrue(error.getMessage().contains(externalFile.toString()));
  }

  private Path writeDefaultFile(String content) throws IOException {
    Path externalFile = temporaryDirectory.resolve("conf/topic-management/db.properties");
    Files.createDirectories(externalFile.getParent());
    Files.writeString(externalFile, content);
    return externalFile.toAbsolutePath().normalize();
  }

  private Properties propertiesFor() {
    Properties properties = new Properties();
    properties.setProperty("catalina.base", temporaryDirectory.toString());
    return properties;
  }
}
