package topicmanagement;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import topicmanagement.entity.User;
import topicmanagement.enums.*;
import topicmanagement.repository.UserRepository;
import topicmanagement.security.CurrentUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:security-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test"
})
@AutoConfigureMockMvc
class IntegratedSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    User lecturer;

    @BeforeEach void setup() {
        users.deleteAll();
        lecturer = new User();
        lecturer.setUserCode("TEST-GV"); lecturer.setUsername("test-lecturer");
        lecturer.setEmail("lecturer@example.test"); lecturer.setFullName("Giảng viên kiểm thử");
        lecturer.setRole(Role.LECTURER); lecturer.setStatus(UserStatus.ACTIVE);
        lecturer.setPasswordHash("unused");
        lecturer = users.save(lecturer);
    }

    MockHttpSession session() {
        var principal = CurrentUser.from(lecturer);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities()));
        var session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        return session;
    }

    @Test void anonymousApiIs401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
    @Test void lecturerCannotAccessAdmin() throws Exception {
        mvc.perform(get("/api/admin/users").session(session())).andExpect(status().isForbidden());
    }
    @Test void existingSessionCannotBypassAccountLock() throws Exception {
        var session = session();
        lecturer.setStatus(UserStatus.LOCKED); users.save(lecturer);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }
    @Test void existingSessionUsesNewRole() throws Exception {
        var session = session();
        lecturer.setRole(Role.STUDENT); users.save(lecturer);
        mvc.perform(get("/api/lecturer/topics").session(session)).andExpect(status().isForbidden());
    }
    @Test void postWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/api/auth/logout").session(session())).andExpect(status().isForbidden());
    }
    @Test void malformedJsonIs400Not500() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content("{bad"))
            .andExpect(status().isBadRequest());
    }
    @Test void missingStaticAssetIs404Not500() throws Exception {
        mvc.perform(get("/assets/not-existing.css")).andExpect(status().isNotFound());
    }
    @Test void loginPageAndCssArePublic() throws Exception {
        mvc.perform(get("/login.html")).andExpect(status().isOk());
        mvc.perform(get("/assets/css/common.css")).andExpect(status().isOk());
    }
}
