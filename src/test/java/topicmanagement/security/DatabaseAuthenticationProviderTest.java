package topicmanagement.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;
import topicmanagement.repository.UserRepository;

class DatabaseAuthenticationProviderTest {

    private DatabaseAuthenticationProvider provider;
    private BCryptPasswordEncoder encoder;
    private StubUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new StubUserRepository();
        encoder = new BCryptPasswordEncoder();
        LegacyPasswordVerifier legacyVerifier = new LegacyPasswordVerifier();
        provider = new DatabaseAuthenticationProvider(userRepository, encoder, legacyVerifier);
    }

    @Test
    void authenticate_successWithUsername() {
        User user = new User();
        user.setId(1L);
        user.setUserCode("GV001");
        user.setUsername("lecturer01");
        user.setEmail("lecturer01@hcmute.edu.vn");
        user.setFullName("Nguyễn Minh Anh");
        user.setPasswordHash(encoder.encode("Demo@12345"));
        user.setRole(Role.LECTURER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.savedUser = user;

        Authentication auth = provider.authenticate(
                new UsernamePasswordAuthenticationToken("lecturer01", "Demo@12345"));

        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals("lecturer01", ((CurrentUser) auth.getPrincipal()).getUsername());
    }

    @Test
    void authenticate_successWithEmail() {
        User user = new User();
        user.setId(1L);
        user.setUserCode("GV001");
        user.setUsername("lecturer01");
        user.setEmail("lecturer01@hcmute.edu.vn");
        user.setFullName("Nguyễn Minh Anh");
        user.setPasswordHash(encoder.encode("Demo@12345"));
        user.setRole(Role.LECTURER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.savedUser = user;

        Authentication auth = provider.authenticate(
                new UsernamePasswordAuthenticationToken("lecturer01@hcmute.edu.vn", "Demo@12345"));

        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
    }

    @Test
    void authenticate_throwsLockedException() {
        User user = new User();
        user.setUsername("locked_user");
        user.setEmail("locked@hcmute.edu.vn");
        user.setStatus(UserStatus.LOCKED);
        userRepository.savedUser = user;

        assertThrows(LockedException.class, () ->
                provider.authenticate(new UsernamePasswordAuthenticationToken("locked_user", "Demo@12345")));
    }

    private static class StubUserRepository extends DummyUserRepository {
        User savedUser;

        @Override
        public Optional<User> findByUsernameOrEmail(String identifier) {
            if (savedUser != null && (identifier.equalsIgnoreCase(savedUser.getUsername()) || identifier.equalsIgnoreCase(savedUser.getEmail()))) {
                return Optional.of(savedUser);
            }
            return Optional.empty();
        }
    }

    private static abstract class DummyUserRepository implements UserRepository {
        @Override public <S extends User> S save(S entity) { return entity; }
        @Override public <S extends User> java.util.List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<User> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public java.util.List<User> findAll() { return null; }
        @Override public java.util.List<User> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(User entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S entity) { return null; }
        @Override public <S extends User> java.util.List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<User> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long aLong) { return null; }
        @Override public User getById(Long aLong) { return null; }
        @Override public User getReferenceById(Long aLong) { return null; }
        @Override public <S extends User> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends User> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends User> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends User> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends User> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends User> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public java.util.List<User> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<User> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public Optional<User> findByUsername(String username) { return Optional.empty(); }
        @Override public Optional<User> findByEmailIgnoreCase(String email) { return Optional.empty(); }
        @Override public java.util.List<User> findByRole(Role role) { return null; }
        @Override public java.util.List<User> findByDepartmentIdAndRoleIn(Long departmentId, java.util.List<Role> roles) { return null; }
        @Override public boolean existsByUsername(String username) { return false; }
        @Override public boolean existsByUserCode(String userCode) { return false; }
        @Override public boolean existsByEmail(String email) { return false; }
    }
}
