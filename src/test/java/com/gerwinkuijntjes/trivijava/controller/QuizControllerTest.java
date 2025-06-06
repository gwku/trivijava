package com.gerwinkuijntjes.trivijava.controller;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.gerwinkuijntjes.trivijava.dto.request.CheckAnswersRequest;
import com.gerwinkuijntjes.trivijava.dto.response.CheckAnswersResponse;
import com.gerwinkuijntjes.trivijava.dto.response.ListQuestionResponse;
import com.gerwinkuijntjes.trivijava.model.Answer;
import com.gerwinkuijntjes.trivijava.model.AnswerResult;
import com.gerwinkuijntjes.trivijava.model.AnsweredQuestion;
import com.gerwinkuijntjes.trivijava.model.Question;
import com.gerwinkuijntjes.trivijava.service.QuestionService;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuizController quizController;

    @Test
    void listQuestions_ShouldReturnQuestions() {
        // Arrange
        String sessionId = "test-session";
        int amount = 5;

        UUID questionId1 = UUID.randomUUID();
        UUID questionId2 = UUID.randomUUID();
        UUID correctAnswerId1 = UUID.randomUUID();
        UUID correctAnswerId2 = UUID.randomUUID();

        Answer correctAnswer1 = new Answer(correctAnswerId1, "Correct answer 1");
        Answer correctAnswer2 = new Answer(correctAnswerId2, "Correct answer 2");

        List<Question> mockQuestions = List.of(
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

        when(questionService.getQuestions(sessionId, amount)).thenReturn(mockQuestions);

        // Act
        ResponseEntity<ListQuestionResponse> response = quizController.listQuestions(sessionId, amount);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sessionId()).isEqualTo(sessionId);
        assertThat(response.getBody().questions()).hasSize(2);
    }

    @Test
    void checkAnswers_ShouldReturnResults() {
        // Arrange
        UUID questionId = UUID.randomUUID();
        UUID answerId = UUID.randomUUID();
        UUID correctAnswerId = UUID.randomUUID();

        List<AnsweredQuestion> answeredQuestions = List.of(
                new AnsweredQuestion(questionId, answerId)
        );
        CheckAnswersRequest request = CheckAnswersRequest.fromDomain(answeredQuestions);

        List<AnswerResult> mockResults = List.of(
                new AnswerResult(questionId, answerId, correctAnswerId)
        );

        when(questionService.checkAnswers(any())).thenReturn(mockResults);

        // Act
        ResponseEntity<CheckAnswersResponse> response = quizController.checkAnswers(request);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(CheckAnswersResponse.fromDomain(mockResults));
    }
} 