package topicmanagement;
import static org.junit.jupiter.api.Assertions.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import topicmanagement.notification.EmailNotificationRepository;
@SpringBootTest
@ActiveProfiles("mysql")
@EnabledIfEnvironmentVariable(named="MYSQL_MIGRATION_TEST",matches="true")
class MySqlMigrationSmokeTest {
    @Autowired Flyway flyway;
    @Autowired EmailNotificationRepository notifications;
    @Test void existingDatabaseUpgradesToV6AndMatchesHibernateEntities() {
        assertEquals("6",flyway.info().current().getVersion().toString());
        assertEquals(0,flyway.migrate().migrationsExecuted);
        assertEquals(0,notifications.count());
    }
}
