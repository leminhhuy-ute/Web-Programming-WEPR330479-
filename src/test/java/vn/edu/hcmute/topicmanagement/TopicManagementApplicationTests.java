package vn.edu.hcmute.topicmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TopicManagementApplicationTests {

    @org.springframework.beans.factory.annotation.Autowired
    private vn.edu.hcmute.topicmanagement.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Autowired
    private vn.edu.hcmute.topicmanagement.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void testSeededUsersPasswordMatches() {
        var users = userRepository.findAll();
        org.junit.jupiter.api.Assertions.assertFalse(users.isEmpty(), "Users should be initialized");
        for (var user : users) {
            boolean matches = passwordEncoder.matches("Password@123", user.getPasswordHash());
            org.junit.jupiter.api.Assertions.assertTrue(matches, "Password@123 must match for user " + user.getUsername());
        }

        var deanDetails = customUserDetailsService.loadUserByUsername("dean");
        org.junit.jupiter.api.Assertions.assertNotNull(deanDetails);

        var studentDetails = customUserDetailsService.loadUserByUsername("24110019");
        org.junit.jupiter.api.Assertions.assertNotNull(studentDetails);
    }
}
