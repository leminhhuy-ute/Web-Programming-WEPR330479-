package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;
import topicmanagement.repository.UserRepository;
import topicmanagement.security.CurrentUser;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:profile-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test"
})
@AutoConfigureMockMvc
class ProfileIntegrationTest {
    private static final AtomicInteger sequence = new AtomicInteger();
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    User student;
    User lecturer;
    User head;
    User dean;

    @BeforeEach void setup() {
        int n = sequence.incrementAndGet();
        student = account("student", n, Role.STUDENT);
        student.setStudentClass("24110CTNB");
        student = users.save(student);
        lecturer = users.save(account("lecturer", n, Role.LECTURER));
        head = users.save(account("head", n, Role.HEAD_OF_DEPT));
        dean = users.save(account("dean", n, Role.DEAN));
    }

    @Test void everyRoleCanSeeOnlyItsOwnProfile() throws Exception {
        for (User user : new User[] {student, lecturer, head, dean}) {
            mvc.perform(get("/api/profile").session(session(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.role").value(user.getRole().name()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
        }
        mvc.perform(get("/api/profile").session(session(student)))
            .andExpect(jsonPath("$.data.studentClass").value("24110CTNB"));
        mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        mvc.perform(get("/profile.html").session(session(student))).andExpect(status().isOk());
    }

    @Test void personalDetailsAreReadOnlyButDeanCanSetStudentClass() throws Exception {
        mvc.perform(put("/api/profile").session(session(student)).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"Tên giả\",\"studentClass\":\"Lớp giả\"}"))
            .andExpect(status().isMethodNotAllowed());

        String body = "{\"userCode\":\"" + student.getUserCode() + "\","
            + "\"username\":\"" + student.getUsername() + "\","
            + "\"fullName\":\"Tên do quản trị cập nhật\","
            + "\"email\":\"" + student.getEmail() + "\","
            + "\"role\":\"STUDENT\",\"departmentId\":null,"
            + "\"status\":\"ACTIVE\",\"password\":null,\"studentClass\":\"24110CTNA\"}";
        mvc.perform(put("/api/admin/users/" + student.getId())
            .session(session(dean)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
        mvc.perform(get("/api/profile").session(session(student)))
            .andExpect(jsonPath("$.data.fullName").value("Tên do quản trị cập nhật"))
            .andExpect(jsonPath("$.data.studentClass").value("24110CTNA"));
    }

    @Test void passwordChangeChecksCurrentPasswordAndExpiresSession() throws Exception {
        MockHttpSession current = session(lecturer);
        mvc.perform(post("/api/profile/password").session(current).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson("wrong", "NewPassword123!")))
            .andExpect(status().isBadRequest());
        assertFalse(current.isInvalid());
        mvc.perform(post("/api/profile/password").session(current).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson("Original123!", "weakpass")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/profile/password").session(current).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson("Original123!", "NewPassword123!")))
            .andExpect(status().isOk());
        assertTrue(current.isInvalid());
        User updated = users.findById(lecturer.getId()).orElseThrow();
        assertTrue(passwords.matches("NewPassword123!", updated.getPasswordHash()));
        assertFalse(passwords.matches("Original123!", updated.getPasswordHash()));
        assertNotNull(updated.getPasswordChangedAt());
    }

    @Test void changingPasswordRequiresCsrf() throws Exception {
        mvc.perform(post("/api/profile/password").session(session(student))
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson("Original123!", "NewPassword123!")))
            .andExpect(status().isForbidden());
    }

    private User account(String kind, int n, Role role) {
        User user = new User();
        user.setUserCode(kind.toUpperCase() + n);
        user.setUsername(kind + n);
        user.setFullName("Người dùng " + kind + n);
        user.setEmail(kind + n + "@example.test");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwords.encode("Original123!"));
        return user;
    }

    private MockHttpSession session(User user) {
        CurrentUser principal = CurrentUser.from(user);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities()));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        return session;
    }

    private String passwordJson(String current, String next) {
        return "{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + next
            + "\",\"confirmPassword\":\"" + next + "\"}";
    }
}
