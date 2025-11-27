package com.example.identity.repository;

import com.example.identity.entity.CV;
import com.example.identity.entity.CVSection;
import com.example.identity.entity.User;
import com.example.identity.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class CVSectionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CVSectionRepository cvSectionRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CVRepository cvRepository;

    private User testUser;
    private CV cv1;
    private CV cv2;

    @BeforeEach
    void setUp() {

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(Role.USER.name()))
                .build();
        entityManager.persist(testUser);

        cv1 = CV.builder()
                .name("CV số 1")
                .user(testUser)
                .build();
        entityManager.persist(cv1);

        cv2 = CV.builder()
                .name("CV số 2 (không có section)")
                .user(testUser)
                .build();
        entityManager.persist(cv2);

        CVSection section1 = CVSection.builder()
                .cv(cv1)
                .sectionType("personal_info")
                .orderIndex(1)
                .build();
        entityManager.persist(section1);

        CVSection section2 = CVSection.builder()
                .cv(cv1)
                .sectionType("experience")
                .orderIndex(2)
                .build();
        entityManager.persist(section2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Test findAllByCvId - Khi CV có section")
    void findAllByCvId_whenSectionsExist() {
        // When
        List<CVSection> result = cvSectionRepository.findAllByCvId(cv1.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSectionType()).isEqualTo("personal_info");
    }

    @Test
    @DisplayName("Test findAllByCvId - Khi CV không có section")
    void findAllByCvId_whenNoSections() {
        // When

        List<CVSection> result = cvSectionRepository.findAllByCvId(cv2.getId());
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Test findAllByCvId - Khi CV ID không tồn tại")
    void findAllByCvId_whenCvDoesNotExist() {
        // When
        List<CVSection> result = cvSectionRepository.findAllByCvId(999L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}