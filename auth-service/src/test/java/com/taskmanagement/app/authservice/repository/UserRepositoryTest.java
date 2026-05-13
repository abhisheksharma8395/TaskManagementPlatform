package com.taskmanagement.app.authservice.repository;

import com.taskmanagement.app.authservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("john_doe");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("$2a$hashed");
        user.setRole("USER");
        user.setActive(true);
        savedUser = userRepository.save(user);
    }

    @Test
    void findByEmail_found_returnsUser() {
        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmail_notFound_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_found_returnsUser() {
        Optional<User> result = userRepository.findByUsername("john_doe");

        assertThat(result).isPresent();
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_nonExistingEmail_returnsFalse() {
        boolean exists = userRepository.existsByEmail("unknown@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_existingUsername_returnsTrue() {
        boolean exists = userRepository.existsByUsername("john_doe");

        assertThat(exists).isTrue();
    }

    @Test
    void findAllByRole_USER_returnsList() {
        List<User> users = userRepository.findAllByRole("USER");

        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getRole()).isEqualTo("USER");
    }

    @Test
    void searchByFullName_found_returnsList() {
        List<User> users = userRepository.searchByFullName("John Doe");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void deleteByUserId_deletes() {
        Long id = savedUser.getUserId();
        userRepository.deleteByUserId(id);

        assertThat(userRepository.findById(id)).isEmpty();
    }
}
