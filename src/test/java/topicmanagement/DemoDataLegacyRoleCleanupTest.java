package topicmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.h2.jdbcx.JdbcDataSource;
import topicmanagement.config.DemoData;
import topicmanagement.repository.*;

import static org.mockito.Mockito.*;

class DemoDataLegacyRoleCleanupTest {
    @Test
    void skipsLegacyUpdateStatementsWhenFreshSchemaHasNoLegacyRows() {
        JdbcTemplate jdbc = jdbc("fresh_" + java.util.UUID.randomUUID().toString().replace("-", ""));
        jdbc.execute("CREATE TABLE users(role ENUM('DEAN','LECTURER','STUDENT'))");
        jdbc.execute("CREATE TABLE announcements(audience ENUM('ALL','LECTURER','STUDENT'))");
        jdbc.update("INSERT INTO users(role) VALUES ('DEAN')");
        jdbc.update("INSERT INTO announcements(audience) VALUES ('ALL')");
        UserRepository users = mock(UserRepository.class);
        when(users.count()).thenReturn(1L); // stop after the cleanup preflight

        newDemoData(users, jdbc).run();

        org.junit.jupiter.api.Assertions.assertEquals("DEAN", jdbc.queryForObject("SELECT role FROM users", String.class));
        org.junit.jupiter.api.Assertions.assertEquals("ALL", jdbc.queryForObject("SELECT audience FROM announcements", String.class));
    }

    @Test
    void remapsLegacyRowsWhenPresent() {
        JdbcTemplate jdbc = jdbc("legacy_" + java.util.UUID.randomUUID().toString().replace("-", ""));
        jdbc.execute("CREATE TABLE users(role ENUM('DEAN','HEAD_OF_DEPT','LECTURER','STUDENT'))");
        jdbc.execute("CREATE TABLE announcements(audience ENUM('ALL','HEAD_OF_DEPT','LECTURER','STUDENT'))");
        jdbc.update("INSERT INTO users(role) VALUES ('HEAD_OF_DEPT')");
        jdbc.update("INSERT INTO announcements(audience) VALUES ('HEAD_OF_DEPT')");
        UserRepository users = mock(UserRepository.class);
        when(users.count()).thenReturn(1L);

        DemoData demo = newDemoData(users, jdbc);
        org.junit.jupiter.api.Assertions.assertSame(jdbc, ReflectionTestUtils.getField(demo, "jdbcTemplate"));
        demo.run();

        org.junit.jupiter.api.Assertions.assertEquals("LECTURER", jdbc.queryForObject("SELECT role FROM users", String.class));
        org.junit.jupiter.api.Assertions.assertEquals("LECTURER", jdbc.queryForObject("SELECT audience FROM announcements", String.class));
    }

    private JdbcTemplate jdbc(String name) {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:" + name + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        source.setUser("sa");
        source.setPassword("");
        return new JdbcTemplate(source);
    }

    private DemoData newDemoData(UserRepository users, JdbcTemplate jdbc) {
        DemoData data = new DemoData(users,
            mock(DepartmentRepository.class),
            mock(RegistrationPeriodRepository.class),
            mock(TopicRepository.class),
            mock(StudentGroupRepository.class),
            mock(GroupMemberRepository.class),
            mock(topicmanagement.council.CouncilRepository.class),
            mock(AnnouncementRepository.class),
            mock(PasswordEncoder.class));
        ReflectionTestUtils.setField(data, "jdbcTemplate", jdbc);
        return data;
    }
}
