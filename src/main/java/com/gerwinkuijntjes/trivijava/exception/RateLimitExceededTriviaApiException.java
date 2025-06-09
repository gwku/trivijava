package com.gerwinkuijntjes.trivijava.exception;

public class RateLimitExceededTriviaApiException extends RuntimeException {
    public RateLimitExceededTriviaApiException(String message) {
        super(message);
    }
}
