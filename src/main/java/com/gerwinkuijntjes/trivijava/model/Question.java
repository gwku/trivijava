package com.gerwinkuijntjes.trivijava.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record Question(
        @NotNull(message = "Question ID cannot be null")
        UUID id,
        
        @NotBlank(message = "Question text cannot be empty")
        @Size(min = 10, max = 500, message = "Question text must be between 10 and 500 characters")
        String question,
        
        @NotBlank(message = "Category cannot be empty")
        String category,
        
        @NotBlank(message = "Difficulty cannot be empty")
        String difficulty,
        
        @NotBlank(message = "Type cannot be empty")
        String type,
        
        @NotNull(message = "Correct answer cannot be null")
        Answer correctAnswer,
        
        @NotNull(message = "Incorrect answers list cannot be null")
        @Size(min = 1, max = 5, message = "There must be between 1 and 5 incorrect answers")
        List<Answer> incorrectAnswers
){
    public List<Answer> allAnswersShuffled() {
        List<Answer> answers = new ArrayList<>(incorrectAnswers);
        answers.add(correctAnswer);
        Collections.shuffle(answers);
        return answers;
    }
}