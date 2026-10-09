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
    @Autowired topicmanagement.repository.AuditLogRepository audit;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Test void existingDatabaseUpgradesToV6AndMatchesHibernateEntities() {
        assertEquals("6",flyway.info().current().getVersion().toString());
        assertEquals(0,flyway.migrate().migrationsExecuted);
        assertEquals(0,notifications.count());
        var entry=new topicmanagement.entity.AuditLog();entry.setAction("MYSQL_MAPPING_TEST");entry.setEntity("AuditLog");
        entry.setOldValue("Nhật ký trước khi sửa ".repeat(5000));entry.setNewValue("Nhật ký sau khi sửa ".repeat(5000));
        audit.saveAndFlush(entry);
        assertEquals("Nhật ký sau khi sửa ".repeat(5000),jdbc.queryForObject("select new_value from audit_logs where id=?",String.class,entry.getId()));
    }
}
