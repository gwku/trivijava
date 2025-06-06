package com.gerwinkuijntjes.trivijava.dto.request;


import java.util.List;
import java.util.UUID;

import com.gerwinkuijntjes.trivijava.model.AnsweredQuestion;

import jakarta.validation.constraints.NotEmpty;

public record CheckAnswersRequest(@NotEmpty List<Answer> answers) {
    record Answer(UUID questionId, UUID answerId) {}

    public List<AnsweredQuestion> toDomain() {
        return answers.stream()
                .map(a -> new AnsweredQuestion(a.questionId(), a.answerId()))
                .toList();
    }

    public static CheckAnswersRequest fromDomain(List<AnsweredQuestion> answeredQuestions) {
        var answers = answeredQuestions.stream()
                .map(q -> new Answer(q.questionId(), q.answerId()))
                .toList();
        return new CheckAnswersRequest(answers);
    }
}
