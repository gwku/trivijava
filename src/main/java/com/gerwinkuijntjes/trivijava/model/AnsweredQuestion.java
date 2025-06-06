package com.gerwinkuijntjes.trivijava.model;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AnsweredQuestion(
        @NotNull(message = "Question ID cannot be null")
        UUID questionId,
        
        @NotNull(message = "Answer ID cannot be null")
        UUID answerId
) {}
