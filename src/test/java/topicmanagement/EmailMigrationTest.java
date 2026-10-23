package topicmanagement;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
class EmailMigrationTest {
    @Test void migrationsUpgradeExistingV5SchemaAndDoNotReapply() throws Exception {
        String url="jdbc:h2:mem:mail-migration;MODE=MySQL;DB_CLOSE_DELAY=-1";
        try(var connection=DriverManager.getConnection(url,"sa","")) {
            connection.createStatement().execute("create table users(id bigint primary key, role varchar(20), constraint ck_users_role check (role in ('DEAN', 'LECTURER', 'HEAD_OF_DEPT', 'STUDENT')))");
            connection.createStatement().execute("create table departments(id bigint primary key)");
            connection.createStatement().execute("create table defenses(id bigint primary key)");
            connection.createStatement().execute("create table announcements(id bigint primary key, audience varchar(30), constraint ck_announcements_audience check (audience in ('ALL', 'STUDENT', 'LECTURER')))");
            connection.createStatement().execute("create table student_reports(id bigint primary key)");
            connection.createStatement().execute("create table topics(id bigint primary key, max_students int not null default 3, constraint ck_topics_size check (max_students between 1 and 3))");
            connection.createStatement().execute("create table student_groups(id bigint primary key, member_count int not null default 1, constraint ck_groups_size check (member_count between 1 and 3))");
        }
        var flyway=Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/mysql")
            .baselineOnMigrate(true).baselineVersion("5").load();
        assertEquals(8,flyway.migrate().migrationsExecuted);
        assertEquals("13",flyway.info().current().getVersion().toString());
        assertEquals(0,flyway.migrate().migrationsExecuted);
        try(var connection=DriverManager.getConnection(url,"sa","")) {
            assertTrue(connection.getMetaData().getTables(null,null,"EMAIL_NOTIFICATIONS",null).next());
            assertTrue(connection.getMetaData().getColumns(null,null,"USERS","STUDENT_CLASS").next());
            assertTrue(connection.getMetaData().getColumns(null,null,"ANNOUNCEMENTS","DEPARTMENT_ID").next());
            assertTrue(connection.getMetaData().getTables(null,null,"ANNOUNCEMENT_READS",null).next());
            assertTrue(connection.getMetaData().getColumns(null,null,"STUDENT_REPORTS","ADVISOR_FEEDBACK").next());
            assertTrue(connection.getMetaData().getColumns(null,null,"STUDENT_REPORTS","STATUS").next());
            assertTrue(connection.getMetaData().getColumns(null,null,"STUDENT_REPORTS","EDIT_COUNT").next());
            assertTrue(connection.getMetaData().getTables(null,null,"STUDENT_REPORT_EDITS",null).next());
            assertTrue(connection.getMetaData().getColumns(null,null,"STUDENT_GROUPS","ADVISOR_COMPLETED_AT").next());
        }
    }
}
