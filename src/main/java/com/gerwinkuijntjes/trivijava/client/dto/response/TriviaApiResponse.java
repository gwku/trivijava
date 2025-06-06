package com.gerwinkuijntjes.trivijava.client.dto.response;

import com.gerwinkuijntjes.trivijava.model.Answer;
import com.gerwinkuijntjes.trivijava.model.Question;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record TriviaApiResponse(
        @JsonProperty("response_code") int responseCode,
        @JsonProperty("results") List<TriviaQuestion> results
) {

    public List<Question> toDomainQuestions() {
        if (results == null) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(TriviaQuestion::toDomain)
                .toList();
    }

    public record TriviaQuestion(
            @JsonProperty("category") String category,
            @JsonProperty("type") String type,
            @JsonProperty("difficulty") String difficulty,
            @JsonProperty("question") String question,
            @JsonProperty("correct_answer") String correctAnswer,
            @JsonProperty("incorrect_answers") List<String> incorrectAnswers
    ) {
        public Question toDomain() {
            UUID questionId = UUID.randomUUID();

            Answer correct = new Answer(UUID.randomUUID(), correctAnswer);

            List<Answer> incorrect = incorrectAnswers == null
                    ? List.of()
                    : incorrectAnswers.stream()
                    .map(ans -> new Answer(UUID.randomUUID(), ans))
                    .toList();

            return new Question(
                    questionId,
                    question,
                    category,
                    difficulty,
                    type,
                    correct,
                    incorrect
            );
        }
    }
}
