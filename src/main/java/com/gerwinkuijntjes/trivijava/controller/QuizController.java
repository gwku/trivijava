package com.gerwinkuijntjes.trivijava.controller;

import com.gerwinkuijntjes.trivijava.dto.request.CheckAnswersRequest;
import com.gerwinkuijntjes.trivijava.dto.response.CheckAnswersResponse;
import com.gerwinkuijntjes.trivijava.dto.response.ListQuestionResponse;
import com.gerwinkuijntjes.trivijava.service.QuestionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("api/v1/quiz")
public class QuizController {

    private final QuestionService questionService;

    public QuizController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("questions")
    public ResponseEntity<ListQuestionResponse> listQuestions(
            @RequestHeader("X-Session-Id") @NotEmpty String sessionId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int amount
    ) {
        var questions = questionService.getQuestions(sessionId, amount);
        var response = ListQuestionResponse.fromDomain(sessionId, questions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("answers")
    public ResponseEntity<CheckAnswersResponse> checkAnswers(@RequestBody CheckAnswersRequest request) {
        var answeredQuestions = request.toDomain();
        var results = questionService.checkAnswers(answeredQuestions);
        var response = CheckAnswersResponse.fromDomain(results);
        return ResponseEntity.ok(response);
    }
}
