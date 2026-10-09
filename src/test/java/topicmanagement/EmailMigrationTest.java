package topicmanagement;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
class EmailMigrationTest {
    @Test void outboxMigrationUpgradesExistingV5SchemaAndDoesNotReapply() throws Exception {
        String url="jdbc:h2:mem:mail-migration;MODE=MySQL;DB_CLOSE_DELAY=-1";
        try(var connection=DriverManager.getConnection(url,"sa","")) {
            connection.createStatement().execute("create table users(id bigint primary key)");
            connection.createStatement().execute("create table defenses(id bigint primary key)");
        }
        var flyway=Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/mysql")
            .baselineOnMigrate(true).baselineVersion("5").load();
        assertEquals(1,flyway.migrate().migrationsExecuted);
        assertEquals("6",flyway.info().current().getVersion().toString());
        assertEquals(0,flyway.migrate().migrationsExecuted);
        try(var connection=DriverManager.getConnection(url,"sa","")) {
            assertTrue(connection.getMetaData().getTables(null,null,"EMAIL_NOTIFICATIONS",null).next());
        }
    }
}
