package com.example.identity.repository;

import com.example.identity.entity.InvalidatedToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class InvalidatedTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    private InvalidatedToken expiredToken;
    private InvalidatedToken validToken;

    @BeforeEach
    void setUp() {
        // --- Chuẩn bị dữ liệu ---

        Instant expiredInstant = Instant.now().minus(1, ChronoUnit.HOURS);
        expiredToken = InvalidatedToken.builder()
                .id("expired-token-id")
                .expirationTime(Date.from(expiredInstant))
                .build();
        entityManager.persist(expiredToken);

        Instant validInstant = Instant.now().plus(1, ChronoUnit.HOURS);
        validToken = InvalidatedToken.builder()
                .id("valid-token-id")
                .expirationTime(Date.from(validInstant))
                .build();
        entityManager.persist(validToken);

        entityManager.flush();
    }

    @Test
    @DisplayName("Test existsById - Khi token tồn tại")
    void existsById_whenTokenExists() {
        // When
        boolean exists = invalidatedTokenRepository.existsById("expired-token-id");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Test existsById - Khi token không tồn tại")
    void existsById_whenTokenDoesNotExist() {
        // When
        boolean exists = invalidatedTokenRepository.existsById("non-existent-id");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Test deleteExpiredTokens - Xóa token hết hạn")
    void deleteExpiredTokens_shouldDeleteOnlyExpired() {
        // Given
        Date threshold = new Date(); //

        // When
        invalidatedTokenRepository.deleteExpiredTokens(threshold);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<InvalidatedToken> foundExpired = invalidatedTokenRepository.findById(String.valueOf("expired-token-id"));
        assertThat(foundExpired).isNotPresent();

        Optional<InvalidatedToken> foundValid = invalidatedTokenRepository.findById(String.valueOf("valid-token-id"));
        assertThat(foundValid).isPresent();
    }
}