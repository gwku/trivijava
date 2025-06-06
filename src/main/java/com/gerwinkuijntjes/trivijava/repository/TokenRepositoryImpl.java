package com.gerwinkuijntjes.trivijava.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TokenRepositoryImpl implements TokenRepository {

    private final ConcurrentHashMap<String, String> tokenStorage = new ConcurrentHashMap<>();

    @Override
    public Optional<String> findToken(String sessionId) {
        return Optional.ofNullable(tokenStorage.get(sessionId));
    }

    @Override
    public void saveToken(String sessionId, String token) {
        tokenStorage.put(sessionId, token);
    }
}
