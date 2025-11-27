package com.example.identity.repository;

import com.example.identity.entity.User;
import com.example.identity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Chuẩn bị một user mẫu
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(Role.USER.name()))
                .build();

        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("Test existsByUsername - Khi user tồn tại")
    void existsByUsername_whenUserExists() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Test existsByUsername - Khi user không tồn tại")
    void existsByUsername_whenUserDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Test existsByEmail - Khi email tồn tại")
    void existsByEmail_whenEmailExists() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Test existsByEmail - Khi email không tồn tại")
    void existsByEmail_whenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Test findByUsername - Khi user tồn tại")
    void findByUsername_whenUserExists() {
        // When
        Optional<User> foundUserOpt = userRepository.findByUsername("testuser");

        // Then
        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUserOpt.get().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Test findByUsername - Khi user không tồn tại")
    void findByUsername_whenUserDoesNotExist() {
        // When
        Optional<User> foundUserOpt = userRepository.findByUsername("nonexistentuser");

        // Then
        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("Test findByEmail - Khi email tồn tại")
    void findByEmail_whenEmailExists() {
        // When
        Optional<User> foundUserOpt = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getEmail()).isEqualTo("test@example.com");
    }
}