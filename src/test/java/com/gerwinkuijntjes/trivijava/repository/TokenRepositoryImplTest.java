package com.gerwinkuijntjes.trivijava.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenRepositoryImplTest {

    private TokenRepositoryImpl tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository = new TokenRepositoryImpl();
    }

    @Test
    void findToken_WithExistingToken_ShouldReturnToken() {
        // Arrange
        String sessionId = "test-session";
        String token = "test-token";
        tokenRepository.saveToken(sessionId, token);

        // Act
        Optional<String> result = tokenRepository.findToken(sessionId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(token);
    }

    @Test
    void findToken_WithNonExistentSession_ShouldReturnEmpty() {
        // Arrange
        String nonExistentSessionId = "non-existent-session";

        // Act
        Optional<String> result = tokenRepository.findToken(nonExistentSessionId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void saveToken_ShouldOverwriteExistingToken() {
        // Arrange
        String sessionId = "test-session";
        String initialToken = "initial-token";
        String newToken = "new-token";

        // Act
        tokenRepository.saveToken(sessionId, initialToken);
        tokenRepository.saveToken(sessionId, newToken);

        // Assert
        Optional<String> result = tokenRepository.findToken(sessionId);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(newToken);
    }

    @Test
    void saveToken_WithMultipleSessions_ShouldStoreSeparately() {
        // Arrange
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        String token1 = "token-1";
        String token2 = "token-2";

        // Act
        tokenRepository.saveToken(sessionId1, token1);
        tokenRepository.saveToken(sessionId2, token2);

        // Assert
        Optional<String> result1 = tokenRepository.findToken(sessionId1);
        Optional<String> result2 = tokenRepository.findToken(sessionId2);

        assertThat(result1).isPresent();
        assertThat(result1.get()).isEqualTo(token1);
        assertThat(result2).isPresent();
        assertThat(result2.get()).isEqualTo(token2);
    }
} 