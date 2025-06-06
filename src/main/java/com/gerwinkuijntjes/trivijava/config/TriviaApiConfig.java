package com.gerwinkuijntjes.trivijava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "trivia.api")
public class TriviaApiConfig {
    private int maxRetries = 3;
    private int rateLimitSeconds = 5;

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRateLimitSeconds() {
        return rateLimitSeconds;
    }

    public void setRateLimitSeconds(int rateLimitSeconds) {
        this.rateLimitSeconds = rateLimitSeconds;
    }
}