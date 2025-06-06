package com.gerwinkuijntjes.trivijava.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gerwinkuijntjes.trivijava.client.TriviaApiClient;
import com.gerwinkuijntjes.trivijava.exception.TokenExhaustedTriviaApiException;
import com.gerwinkuijntjes.trivijava.exception.TokenNotFoundTriviaApiException;
import com.gerwinkuijntjes.trivijava.model.Answer;
import com.gerwinkuijntjes.trivijava.model.AnswerResult;
import com.gerwinkuijntjes.trivijava.model.AnsweredQuestion;
import com.gerwinkuijntjes.trivijava.model.Question;
import com.gerwinkuijntjes.trivijava.repository.QuestionRepository;
import com.gerwinkuijntjes.trivijava.repository.TokenRepository;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    
    @Mock
    private TokenRepository tokenRepository;
    
    @Mock
    private TriviaApiClient triviaClient;
    
    private QuestionService questionService;
    
    @BeforeEach
    void setUp() {
        questionService = new QuestionService(questionRepository, tokenRepository, triviaClient);
    }
    
    @Test
    void getQuestions_WithExistingToken_ShouldReturnQuestions() {
        // Arrange
        String sessionId = "test-session";
        String token = "existing-token";
        int amount = 5;
        
        UUID questionId1 = UUID.randomUUID();
        UUID questionId2 = UUID.randomUUID();
        UUID correctAnswerId1 = UUID.randomUUID();
        UUID correctAnswerId2 = UUID.randomUUID();
        
        Answer correctAnswer1 = new Answer(correctAnswerId1, "Correct answer 1");
        Answer correctAnswer2 = new Answer(correctAnswerId2, "Correct answer 2");
        
        List<Question> expectedQuestions = List.of(
            new Question(
                questionId1,
                "Test question 1",
                "Science",
                "medium",
                "multiple",
                correctAnswer1,
                List.of(new Answer(UUID.randomUUID(), "Wrong answer 1"))
            ),
            new Question(
                questionId2,
                "Test question 2",
                "History",
                "easy",
                "multiple",
                correctAnswer2,
                List.of(new Answer(UUID.randomUUID(), "Wrong answer 2"))
            )
        );
        
        when(tokenRepository.findToken(sessionId)).thenReturn(Optional.of(token));
        when(triviaClient.fetchQuestions(token, amount)).thenReturn(expectedQuestions);
        
        // Act
        List<Question> result = questionService.getQuestions(sessionId, amount);
        
        // Assert
        assertThat(result).isEqualTo(expectedQuestions);
        verify(questionRepository, times(2)).storeCorrectAnswer(any(), any());
    }
    
    @Test
    void getQuestions_WithNoToken_ShouldRequestNewToken() {
        // Arrange
        String sessionId = "test-session";
        String newToken = "new-token";
        int amount = 5;
        
        UUID questionId = UUID.randomUUID();
        UUID correctAnswerId = UUID.randomUUID();
        Answer correctAnswer = new Answer(correctAnswerId, "Correct answer");
        
        List<Question> expectedQuestions = List.of(
            new Question(
                questionId,
                "Test question 1",
                "Science",
                "medium",
                "multiple",
                correctAnswer,
                List.of(new Answer(UUID.randomUUID(), "Wrong answer"))
            )
        );
        
        when(tokenRepository.findToken(sessionId)).thenReturn(Optional.empty());
        when(triviaClient.requestNewToken()).thenReturn(newToken);
        when(triviaClient.fetchQuestions(newToken, amount)).thenReturn(expectedQuestions);
        
        // Act
        List<Question> result = questionService.getQuestions(sessionId, amount);
        
        // Assert
        assertThat(result).isEqualTo(expectedQuestions);
        verify(tokenRepository).saveToken(sessionId, newToken);
        verify(questionRepository).storeCorrectAnswer(any(), any());
    }
    
    @Test
    void getQuestions_WithTokenNotFound_ShouldRequestNewToken() {
        // Arrange
        String sessionId = "test-session";
        String oldToken = "old-token";
        String newToken = "new-token";
        int amount = 5;
        
        UUID questionId = UUID.randomUUID();
        UUID correctAnswerId = UUID.randomUUID();
        Answer correctAnswer = new Answer(correctAnswerId, "Correct answer");
        
        List<Question> expectedQuestions = List.of(
            new Question(
                questionId,
                "Test question 1",
                "Science",
                "medium",
                "multiple",
                correctAnswer,
                List.of(new Answer(UUID.randomUUID(), "Wrong answer"))
            )
        );
        
        when(tokenRepository.findToken(sessionId)).thenReturn(Optional.of(oldToken));
        when(triviaClient.fetchQuestions(oldToken, amount)).thenThrow(new TokenNotFoundTriviaApiException("Token not found"));
        when(triviaClient.requestNewToken()).thenReturn(newToken);
        when(triviaClient.fetchQuestions(newToken, amount)).thenReturn(expectedQuestions);
        
        // Act
        List<Question> result = questionService.getQuestions(sessionId, amount);
        
        // Assert
        assertThat(result).isEqualTo(expectedQuestions);
        verify(tokenRepository).saveToken(sessionId, newToken);
        verify(questionRepository).storeCorrectAnswer(any(), any());
        verify(triviaClient).fetchQuestions(newToken, amount);
    }
    
    @Test
    void getQuestions_WithExhaustedToken_ShouldResetToken() {
        // Arrange
        String sessionId = "test-session";
        String oldToken = "old-token";
        String resetToken = "reset-token";
        int amount = 5;
        
        UUID questionId = UUID.randomUUID();
        UUID correctAnswerId = UUID.randomUUID();
        Answer correctAnswer = new Answer(correctAnswerId, "Correct answer");
        
        List<Question> expectedQuestions = List.of(
            new Question(
                questionId,
                "Test question 1",
                "Science",
                "medium",
                "multiple",
                correctAnswer,
                List.of(new Answer(UUID.randomUUID(), "Wrong answer"))
            )
        );
        
        when(tokenRepository.findToken(sessionId)).thenReturn(Optional.of(oldToken));
        when(triviaClient.fetchQuestions(oldToken, amount)).thenThrow(new TokenExhaustedTriviaApiException("Token exhausted"));
        when(triviaClient.resetToken(oldToken)).thenReturn(resetToken);
        when(triviaClient.fetchQuestions(resetToken, amount)).thenReturn(expectedQuestions);
        
        // Act
        List<Question> result = questionService.getQuestions(sessionId, amount);
        
        // Assert
        assertThat(result).isEqualTo(expectedQuestions);
        verify(questionRepository).storeCorrectAnswer(any(), any());
    }
    
    @Test
    void checkAnswers_ShouldReturnResults() {
        // Arrange
        UUID questionId = UUID.randomUUID();
        UUID answerId = UUID.randomUUID();
        UUID correctAnswerId = UUID.randomUUID();
        
        AnsweredQuestion answeredQuestion = new AnsweredQuestion(questionId, answerId);
        when(questionRepository.getCorrectAnswer(questionId)).thenReturn(Optional.of(correctAnswerId));
        
        // Act
        List<AnswerResult> results = questionService.checkAnswers(List.of(answeredQuestion));
        
        // Assert
        List<AnswerResult> expectedResults = List.of(
            new AnswerResult(questionId, answerId, correctAnswerId)
        );
        assertThat(results).isEqualTo(expectedResults);
    }
    
    @Test
    void checkAnswers_WithMissingCorrectAnswer_ShouldFilterOutResult() {
        // Arrange
        UUID questionId = UUID.randomUUID();
        UUID answerId = UUID.randomUUID();
        
        AnsweredQuestion answeredQuestion = new AnsweredQuestion(questionId, answerId);
        when(questionRepository.getCorrectAnswer(questionId)).thenReturn(Optional.empty());
        
        // Act
        List<AnswerResult> results = questionService.checkAnswers(List.of(answeredQuestion));
        
        // Assert
        assertThat(results).isEmpty();
    }
} 