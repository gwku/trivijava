package com.gerwinkuijntjes.trivijava.dto.response;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gerwinkuijntjes.trivijava.model.AnswerResult;

public record CheckAnswersResponse(List<Result> results) {
    record Result(UUID questionId, UUID givenAnswerId, UUID correctAnswerId) {
        @JsonProperty("isCorrect")
        public boolean isCorrect() {
            return givenAnswerId.equals(correctAnswerId);
        }

        static Result fromDomain(AnswerResult answerResult) {
            return new Result(
                    answerResult.questionId(),
                    answerResult.givenAnswerId(),
                    answerResult.correctAnswerId()
            );
        }
    }

    public static CheckAnswersResponse fromDomain(List<AnswerResult> results) {
        var dtoResults = results.stream()
                .map(Result::fromDomain)
                .toList();
        return new CheckAnswersResponse(dtoResults);
    }
}
