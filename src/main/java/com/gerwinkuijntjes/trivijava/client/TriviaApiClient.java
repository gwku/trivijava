package com.gerwinkuijntjes.trivijava.client;

import com.gerwinkuijntjes.trivijava.client.dto.response.TriviaApiResponse;
import com.gerwinkuijntjes.trivijava.config.TriviaApiConfig;
import com.gerwinkuijntjes.trivijava.exception.*;
import com.gerwinkuijntjes.trivijava.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class TriviaApiClient {
    private static final Logger logger = LoggerFactory.getLogger(TriviaApiClient.class);
    private final WebClient webClient;
    private final TriviaApiConfig config;

    public TriviaApiClient(WebClient webClient, TriviaApiConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    public CompletableFuture<List<Question>> fetchQuestionsAsync(String sessionId, int amount) {
        return webClient.get()
                .uri(uri -> uri.path("/api.php")
                        .queryParam("amount", amount)
                        .queryParam("token", sessionId)
                        .build())
                .retrieve()
                .bodyToMono(TriviaApiResponse.class)
                .doOnError(ex -> logger.error("Error during API request", ex))
                .flatMap(this::handleResponse)
                .retryWhen(retrySpec())
                .onErrorResume(this::handleError)
                .toFuture();
    }

    private Mono<List<Question>> handleResponse(TriviaApiResponse response) {
        if (response == null) {
            logger.warn("Received null response from Trivia API");
            return Mono.just(Collections.emptyList());
        }

        return switch (response.responseCode()) {
            case 0 -> Mono.just(response.toDomainQuestions());
            case 1 -> {
                logger.info("Trivia API: No results (code {}).", response.responseCode());
                yield Mono.just(Collections.emptyList());
            }
            case 2 -> {
                logger.info("Trivia API: Invalid parameters (code {}).", response.responseCode());
                yield Mono.just(Collections.emptyList());
            }
            case 3 -> Mono.error(new TokenNotFoundTriviaApiException("Token not found"));
            case 4 -> Mono.error(new TokenExhaustedTriviaApiException("Token exhausted"));
            case 5 -> {
                logger.warn("Rate limit exceeded");
                yield Mono.error(new RateLimitExceededTriviaApiException("Rate limit exceeded"));
            }
            default -> {
                logger.warn("Unknown response code: {}", response.responseCode());
                yield Mono.just(Collections.emptyList());
            }
        };
    }

    private Retry retrySpec() {
        return Retry.fixedDelay(config.getMaxRetries(), Duration.ofSeconds(config.getRateLimitSeconds()))
                .filter(this::shouldRetry)
                .doBeforeRetry(retry -> logger.warn("Retrying ({}/{}): {}",
                        retry.totalRetries() + 1, config.getMaxRetries(), retry.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) -> {
                    logger.error("All retries exhausted");
                    return new IllegalStateException("Rate limit exceeded after retries");
                });
    }

    private boolean shouldRetry(Throwable ex) {
        return ex instanceof RateLimitExceededTriviaApiException ||
                (ex instanceof WebClientResponseException webEx && webEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS);
    }

    private Mono<List<Question>> handleError(Throwable ex) {
        if (ex instanceof TokenNotFoundTriviaApiException || ex instanceof TokenExhaustedTriviaApiException)
            return Mono.error(ex);

        logger.error("Trivia API error", ex);
        return Mono.just(Collections.emptyList());
    }

    public CompletableFuture<String> requestNewTokenAsync() {
        return fetchToken("request", Optional.empty());
    }

    public CompletableFuture<String> resetTokenAsync(String token) {
        return fetchToken("reset", Optional.of(token));
    }

    private CompletableFuture<String> fetchToken(String command, Optional<String> token) {
        return webClient.get()
                .uri(uri -> uri.path("/api_token.php")
                        .queryParam("command", command)
                        .queryParamIfPresent("token", token)
                        .build())
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .<String>handle((res, sink) -> {
                    if (res == null || res.responseCode != 0) {
                        logger.error("Token command '{}' failed: {}", command, res != null ? res.responseMessage : "null response");
                        sink.error(new IllegalStateException("Failed token operation"));
                    } else {
                        sink.next(res.token);
                    }
                })
                .onErrorMap(ex -> {
                    logger.error("Error during '{}' token command", command, ex);
                    return new IllegalStateException("Token operation failed", ex);
                })
                .toFuture();
    }

    private record TokenResponse(int responseCode, String responseMessage, String token) {
    }
}
