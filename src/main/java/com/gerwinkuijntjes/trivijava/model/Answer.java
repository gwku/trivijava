package com.gerwinkuijntjes.trivijava.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record Answer(
        @NotNull(message = "Answer ID cannot be null")
        UUID id,
        
        @NotBlank(message = "Answer text cannot be empty")
        @Size(min = 1, max = 200, message = "Answer text must be between 1 and 200 characters")
        String answer
){}
