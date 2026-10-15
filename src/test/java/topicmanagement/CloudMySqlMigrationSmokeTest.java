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
        assertEquals("7", flyway.info().current().getVersion().toString());
        assertEquals(3, flyway.info().applied().length); // V1, V6, V7, no manual baseline
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(13, users.count());
        assertEquals(0L, jdbc.queryForObject("select count(*) from email_notifications", Long.class));

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
