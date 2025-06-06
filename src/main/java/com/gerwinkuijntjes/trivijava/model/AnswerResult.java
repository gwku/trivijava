package com.gerwinkuijntjes.trivijava.model;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AnswerResult(
        @NotNull(message = "Question ID cannot be null")
        UUID questionId,
        
        @NotNull(message = "Given answer ID cannot be null")
        UUID givenAnswerId,
        
        @NotNull(message = "Correct answer ID cannot be null")
        UUID correctAnswerId
) {}
