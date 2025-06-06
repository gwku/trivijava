package com.gerwinkuijntjes.trivijava.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionRepositoryImplTest {

    private QuestionRepositoryImpl questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository = new QuestionRepositoryImpl();
    }

    @Test
    void storeCorrectAnswer_ShouldStoreAnswer() {
        // Arrange
        UUID questionId = UUID.randomUUID();
        UUID correctAnswerId = UUID.randomUUID();

        // Act
        questionRepository.storeCorrectAnswer(questionId, correctAnswerId);

        // Assert
        Optional<UUID> result = questionRepository.getCorrectAnswer(questionId);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(correctAnswerId);
    }

    @Test
    void getCorrectAnswer_WithNonExistentQuestion_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistentQuestionId = UUID.randomUUID();

        // Act
        Optional<UUID> result = questionRepository.getCorrectAnswer(nonExistentQuestionId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void storeCorrectAnswer_ShouldOverwriteExistingAnswer() {
        // Arrange
        UUID questionId = UUID.randomUUID();
        UUID initialAnswerId = UUID.randomUUID();
        UUID newAnswerId = UUID.randomUUID();

        // Act
        questionRepository.storeCorrectAnswer(questionId, initialAnswerId);
        questionRepository.storeCorrectAnswer(questionId, newAnswerId);

        // Assert
        Optional<UUID> result = questionRepository.getCorrectAnswer(questionId);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(newAnswerId);
    }
} 