package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import topicmanagement.config.DemoData;
import topicmanagement.repository.UserRepository;

/** Runs against a separate EMPTY MySQL database, never the existing V5 fixture. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"cloud", "cloud-demo"})
@EnabledIfEnvironmentVariable(named = "CLOUD_MYSQL_TEST", matches = "true")
class CloudMySqlMigrationSmokeTest {
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired DemoData demo;
    @Autowired MockMvc mvc;

    @Test void emptyDatabaseMigratesAndDemoSeedingDoesNotResetUsers() throws Exception {
        assertEquals("12", flyway.info().current().getVersion().toString());
        assertEquals(8, flyway.info().applied().length); // V1, V6, V7, V8, V9, V10, V11, V12; no manual baseline
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(13, users.count());
        assertEquals(0L, jdbc.queryForObject("select count(*) from email_notifications", Long.class));

        // Direct post-migration verification:
        // 1. No users have role 'HEAD_OF_DEPT'
        assertEquals(0L, jdbc.queryForObject("select count(*) from users where role = 'HEAD_OF_DEPT'", Long.class));

        // 2. No announcements have audience 'HEAD_OF_DEPT'
        assertEquals(0L, jdbc.queryForObject("select count(*) from announcements where audience = 'HEAD_OF_DEPT'", Long.class));

        // 3. Constraint ck_users_role rejects 'HEAD_OF_DEPT'
        assertThrows(Exception.class, () ->
            jdbc.update("insert into users(user_code, username, password_hash, full_name, email, role, status) values (?, ?, ?, ?, ?, ?, ?)",
                "INVALID_ROLE", "invalid_role_user", "hash", "Invalid Role", "invalid@test.com", "HEAD_OF_DEPT", "ACTIVE")
        );

        // 4. Constraint ck_announcements_audience rejects 'HEAD_OF_DEPT'
        Long deanId = users.findByUsername("dean01").orElseThrow().getId();
        assertThrows(Exception.class, () ->
            jdbc.update("insert into announcements(title, content, audience, created_by) values (?, ?, ?, ?)",
                "Test Invalid Audience", "Content", "HEAD_OF_DEPT", deanId)
        );

        jdbc.update("update users set student_class=? where username=?", "24110CTNB", "student01");
        demo.run();
        assertEquals(13, users.count());
        assertEquals("24110CTNB", users.findByUsername("student01").orElseThrow().getStudentClass());
        try (var connection = jdbc.getDataSource().getConnection()) {
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:mysql:"));
        }
    }

    @Test void seededDeanCanLoginAndReadProfileWithSecurityEnabled() throws Exception {
        mvc.perform(get("/login.html")).andExpect(status().isOk());
        var result = mvc.perform(post("/api/auth/login").secure(true).with(csrf())
            .contentType("application/json")
            .content("{\"username\":\"dean01\",\"password\":\"Demo@12345\"}"))
            .andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/api/profile").secure(true).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("dean01"))
            .andExpect(jsonPath("$.data.role").value("DEAN"));
    }
}
