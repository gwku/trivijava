package com.gerwinkuijntjes.trivijava.service;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gerwinkuijntjes.trivijava.client.TriviaApiClient;
import com.gerwinkuijntjes.trivijava.exception.TokenExhaustedTriviaApiException;
import com.gerwinkuijntjes.trivijava.exception.TokenNotFoundTriviaApiException;
import com.gerwinkuijntjes.trivijava.model.AnswerResult;
import com.gerwinkuijntjes.trivijava.model.AnsweredQuestion;
import com.gerwinkuijntjes.trivijava.model.Question;
import com.gerwinkuijntjes.trivijava.repository.QuestionRepository;
import com.gerwinkuijntjes.trivijava.repository.TokenRepository;

@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final TokenRepository tokenRepository;
    private final TriviaApiClient triviaClient;

    public QuestionService(QuestionRepository questionRepository, TokenRepository tokenRepository, TriviaApiClient triviaClient) {
        this.questionRepository = questionRepository;
        this.tokenRepository = tokenRepository;
        this.triviaClient = triviaClient;
    }

    public List<Question> getQuestions(String sessionId, int amount) {
        var token = tokenRepository.findToken(sessionId)
                .orElseGet(() -> {
                    var newToken = triviaClient.requestNewTokenAsync().join();
                    tokenRepository.saveToken(sessionId, newToken);
                    return newToken;
                });

        try {
            return getQuestionsAndSaveCorrectAnswer(token, amount);
        } catch (TokenNotFoundTriviaApiException ex) {
            logger.warn("Token not found. Requesting new token...");
            var newToken = triviaClient.requestNewTokenAsync().join();
            tokenRepository.saveToken(sessionId, newToken);
            return getQuestionsAndSaveCorrectAnswer(newToken, amount);
        } catch (TokenExhaustedTriviaApiException ex) {
            logger.info("Token exhausted. Resetting token...");
            var resetToken = triviaClient.resetTokenAsync(token).join();
            return getQuestionsAndSaveCorrectAnswer(resetToken, amount);
        }
    }

    public List<Question> getQuestionsAndSaveCorrectAnswer(String token, int amount) {
        var questions = triviaClient.fetchQuestionsAsync(token, amount).join();
        questions.forEach(question -> questionRepository.storeCorrectAnswer(question.id(), question.correctAnswer().id()));
        return questions;
    }

    public List<AnswerResult> checkAnswers(List<AnsweredQuestion> answeredQuestions) {
        return answeredQuestions.stream()
                .map(q -> {
                    var correctId = questionRepository.getCorrectAnswer(q.questionId());
                    return correctId.map(uuid -> new AnswerResult(q.questionId(), q.answerId(), uuid)).orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
