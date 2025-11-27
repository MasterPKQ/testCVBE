package com.example.identity.repository;

import com.example.identity.entity.CV;
import com.example.identity.entity.User;
import com.example.identity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CVRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CVRepository cvRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;
    private CV cv1;

    @BeforeEach
    void setUp() {

        // Tạo User 1 (có CV)
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(Role.USER.name()))
                .build();
        entityManager.persist(testUser);

        // Tạo User 2 (không có CV)
        anotherUser = User.builder()
                .username("anotheruser")
                .email("another@example.com")
                .password("pass")
                .firstName("Another")
                .lastName("User")
                .roles(Set.of(Role.USER.name()))
                .build();
        entityManager.persist(anotherUser);

        cv1 = CV.builder()
                .name("CV của testuser")
                .user(testUser)
                .build();
        entityManager.persist(cv1);

        entityManager.flush();
    }

    @Test
    @DisplayName("Test findAllByUserUsername - Khi user có CV")
    void findAllByUserUsername_whenCVsExist() {
        // When
        List<CV> result = cvRepository.findAllByUserUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("CV của testuser");
    }

    @Test
    @DisplayName("Test findAllByUserUsername - Khi user tồn tại nhưng không có CV")
    void findAllByUserUsername_whenUserHasNoCVs() {
        // When
        List<CV> result = cvRepository.findAllByUserUsername("anotheruser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Test findAllByUserUsername - Khi user không tồn tại")
    void findAllByUserUsername_whenUserDoesNotExist() {
        // When
        List<CV> result = cvRepository.findAllByUserUsername("nonexistentuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}