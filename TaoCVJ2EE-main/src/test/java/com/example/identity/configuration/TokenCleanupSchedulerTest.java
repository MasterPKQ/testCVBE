package com.example.identity.configuration;

import com.example.identity.repository.InvalidatedTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenCleanupSchedulerTest {

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Test
    @DisplayName("Should call deleteExpiredTokens with correct threshold")
    void cleanupExpiredTokens_shouldCallRepository() {
        // Given
        doNothing().when(invalidatedTokenRepository).deleteExpiredTokens(any(Date.class));

        // When
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

        verify(invalidatedTokenRepository, times(1)).deleteExpiredTokens(dateCaptor.capture());

        assertThat(dateCaptor.getValue()).isBefore(new Date());
    }
}