package com.gerwinkuijntjes.trivijava.dto.response;

import java.util.List;
import java.util.UUID;

public record ListQuestionResponse(String sessionId, List<Question> questions) {
    record Question(UUID id, String question, List<Answer> answers) {
        static Question fromDomain(com.gerwinkuijntjes.trivijava.model.Question domainQuestion) {
            var answers = domainQuestion.allAnswersShuffled().stream()
                    .map(a -> new Answer(a.id(), a.answer()))
                    .toList();
            return new Question(domainQuestion.id(), domainQuestion.question(), answers);
        }
    }
    record Answer(UUID answerId, String answer) {}

    public static ListQuestionResponse fromDomain(String sessionId, List<com.gerwinkuijntjes.trivijava.model.Question> questions) {
        var questionResponses = questions.stream()
                .map(Question::fromDomain)
                .toList();

        return new ListQuestionResponse(sessionId, questionResponses);
    }
}




