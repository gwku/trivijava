package com.gerwinkuijntjes.trivijava.repository;


import java.util.Optional;

public interface TokenRepository {
    Optional<String> findToken(String sessionId);
    void saveToken(String sessionId, String token);
}

