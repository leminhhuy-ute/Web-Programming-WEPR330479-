package topicmanagement.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

final class DatabaseConfigLoader {
  private static final Logger log = Logger.getLogger(DatabaseConfigLoader.class.getName());
  private static final String DEFAULT_FILE = "conf/topic-management/db.properties";

  private DatabaseConfigLoader() {}

  static DatabaseSettings load() {
    return load(System.getProperties(), System.getenv());
  }

  static DatabaseSettings load(Properties systemProperties, Map<String, String> environment) {
    Path externalFile = resolveExternalFile(systemProperties, environment);
    Properties externalProperties = readExternalProperties(externalFile);

    LocatedValue url = value("DB_URL", "db.url", systemProperties, environment, externalProperties);
    LocatedValue username =
        value("DB_USERNAME", "db.username", systemProperties, environment, externalProperties);
    LocatedValue password =
        value("DB_PASSWORD", "db.password", systemProperties, environment, externalProperties);

    List<String> missing = new ArrayList<>();
    if (url == null) missing.add("db.url");
    if (username == null) missing.add("db.username");
    if (password == null) missing.add("db.password");
    if (!missing.isEmpty()) {
      throw new IllegalStateException(
          "Thiếu cấu hình database bắt buộc: "
              + String.join(", ", missing)
              + ". Đã kiểm tra JVM system properties, environment variables và file external: "
              + externalFile);
    }

    Set<String> sources = new LinkedHashSet<>();
    sources.add(url.source());
    sources.add(username.source());
    sources.add(password.source());
    String sourceDescription = String.join(", ", sources);
    if (sources.contains("external file")) {
      log.info("Database configuration loaded from external file: " + externalFile + ".");
    }
    if (sources.size() > 1 || !sources.contains("external file")) {
      log.info("Database configuration resolved from " + sourceDescription + ".");
    }
    return new DatabaseSettings(url.value(), username.value(), password.value(), externalFile, sourceDescription);
  }

  private static Path resolveExternalFile(Properties systemProperties, Map<String, String> environment) {
    String configuredPath =
        first(
            systemProperties.getProperty("db.config.file"),
            systemProperties.getProperty("DB_CONFIG_FILE"),
            environment.get("DB_CONFIG_FILE"));
    if (configuredPath != null) {
      try {
        return Path.of(configuredPath).toAbsolutePath().normalize();
      } catch (InvalidPathException e) {
        throw new IllegalStateException("Đường dẫn db.config.file không hợp lệ.", e);
      }
    }

    String catalinaBase = first(systemProperties.getProperty("catalina.base"), environment.get("CATALINA_BASE"));
    if (catalinaBase != null) {
      return Path.of(catalinaBase, "conf", "topic-management", "db.properties")
          .toAbsolutePath()
          .normalize();
    }
    return Path.of(DEFAULT_FILE).toAbsolutePath().normalize();
  }

  private static Properties readExternalProperties(Path externalFile) {
    Properties properties = new Properties();
    if (!Files.isRegularFile(externalFile)) return properties;
    try (InputStream input = Files.newInputStream(externalFile)) {
      properties.load(input);
      return properties;
    } catch (IOException e) {
      throw new IllegalStateException("Không thể đọc file cấu hình database: " + externalFile, e);
    }
  }

  private static LocatedValue value(
      String environmentKey,
      String propertyKey,
      Properties systemProperties,
      Map<String, String> environment,
      Properties externalProperties) {
    String systemValue = first(systemProperties.getProperty(environmentKey), systemProperties.getProperty(propertyKey));
    if (systemValue != null) return new LocatedValue(systemValue, "JVM system properties");

    String environmentValue = environment.get(environmentKey);
    if (notBlank(environmentValue)) return new LocatedValue(environmentValue, "environment variables");

    String externalValue = externalProperties.getProperty(propertyKey);
    if (notBlank(externalValue)) return new LocatedValue(externalValue, "external file");
    return null;
  }

  private static String first(String... candidates) {
    for (String candidate : candidates) {
      if (notBlank(candidate)) return candidate;
    }
    return null;
  }

  private static boolean notBlank(String value) {
    return value != null && !value.isBlank();
  }

  record DatabaseSettings(
      String url, String username, String password, Path externalFile, String sourceDescription) {}

  private record LocatedValue(String value, String source) {}
}
