package com.gerwinkuijntjes.trivijava.client;

import com.gerwinkuijntjes.trivijava.client.dto.response.TriviaApiResponse;
import com.gerwinkuijntjes.trivijava.config.TriviaApiConfig;
import com.gerwinkuijntjes.trivijava.exception.TokenExhaustedTriviaApiException;
import com.gerwinkuijntjes.trivijava.exception.TokenNotFoundTriviaApiException;
import com.gerwinkuijntjes.trivijava.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class TriviaApiClient {
    private static final Logger logger = LoggerFactory.getLogger(TriviaApiClient.class);
    private final RestTemplate restTemplate;
    private final TriviaApiConfig properties;

    private Instant lastRequestTime = Instant.EPOCH;

    public TriviaApiClient(RestTemplate restTemplate, TriviaApiConfig properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public synchronized List<Question> fetchQuestions(String sessionId, int amount) {
        int attempt = 0;

        while (attempt < properties.getMaxRetries()) {
            if (attempt > 0) {
                enforceRateLimit();  // Wait only after first attempt
            }

            var uri = UriComponentsBuilder.fromUriString("https://opentdb.com/api.php")
                    .queryParam("amount", amount)
                    .queryParam("token", sessionId)
                    .build()
                    .toUri();

            try {
                TriviaApiResponse response = restTemplate.getForObject(uri, TriviaApiResponse.class);
                lastRequestTime = Instant.now();

                if (response == null) {
                    logger.warn("Received null response from Trivia API");
                    return Collections.emptyList();
                }

                switch (response.responseCode()) {
                    case 0:
                        return response.toDomainQuestions();
                    case 1:
                        logger.info("Trivia API: No results for this query.");
                        return Collections.emptyList();
                    case 2:
                        logger.warn("Trivia API: Invalid parameters");
                        return Collections.emptyList();
                    case 3:
                        throw new TokenNotFoundTriviaApiException("Trivia API: Token not found");
                    case 4:
                        throw new TokenExhaustedTriviaApiException("Trivia API: Token exhausted, reset needed");
                    case 5:
                        attempt++;
                        if (attempt >= properties.getMaxRetries()) {
                            throw new IllegalStateException("Trivia API: Rate limit exceeded after retries");
                        }
                        logger.warn("Trivia API: Rate limit exceeded (code 5), retrying (attempt {}/{})", attempt, properties.getMaxRetries());
                        break;
                    default:
                        logger.warn("Trivia API: Unknown response code {}", response.responseCode());
                        return Collections.emptyList();
                }

            } catch (HttpClientErrorException.TooManyRequests ex) {
                attempt++;
                if (attempt >= properties.getMaxRetries()) {
                    logger.error("HTTP 429 Too Many Requests after retries", ex);
                    return Collections.emptyList();
                }
                logger.warn("HTTP 429 Too Many Requests, retrying (attempt {}/{})", attempt, properties.getMaxRetries());
            } catch (HttpClientErrorException | HttpServerErrorException ex) {
                logger.error("HTTP error from Trivia API: {}", ex.getStatusCode(), ex);
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private record TokenResponse(int responseCode, String responseMessage, String token) {
    }

    public String requestNewToken() {
        var uri = "https://opentdb.com/api_token.php?command=request";
        try {
            var response = restTemplate.getForObject(uri, TokenResponse.class);
            if (response == null || response.responseCode() != 0) {
                logger.error("Failed to obtain new token from Trivia API: response invalid or null");
                throw new IllegalStateException("Failed to obtain new token from Trivia API");
            }
            return response.token();
        } catch (Exception ex) {
            logger.error("Exception while requesting new token from Trivia API", ex);
            throw new IllegalStateException("Exception during token request", ex);
        }
    }

    public String resetToken(String token) {
        var uri = "https://opentdb.com/api_token.php?command=reset&token=" + token;
        try {
            var response = restTemplate.getForObject(uri, TokenResponse.class);
            if (response == null || response.responseCode() != 0) {
                logger.error("Failed to reset token: invalid response or null");
                throw new IllegalStateException("Failed to reset token");
            }

            return response.token();
        } catch (Exception ex) {
            logger.error("Exception while resetting token", ex);
            throw new IllegalStateException("Exception during token reset", ex);
        }
    }

    private void enforceRateLimit() {
        long secondsSinceLast = Instant.now().getEpochSecond() - lastRequestTime.getEpochSecond();
        int requiredWait = properties.getRateLimitSeconds();

        if (secondsSinceLast >= requiredWait)
            return;

        long sleepMillis = (requiredWait - secondsSinceLast) * 1000L;
        logger.info("Rate limit: Sleeping {} ms to respect rate limit", sleepMillis);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Rate-limit sleep interrupted");
        }
    }
}

