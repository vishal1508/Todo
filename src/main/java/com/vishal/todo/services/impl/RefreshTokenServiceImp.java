package com.vishal.todo.services.impl;

import com.vishal.todo.entity.User;
import com.vishal.todo.exception.GlobalExceptionHandler;
import com.vishal.todo.exception.RefreshTokenException;
import com.vishal.todo.repositories.RefreshTokenRepository;
import com.vishal.todo.security.RefreshToken;
import com.vishal.todo.services.RefreshTokenService;
import com.vishal.todo.utils.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenServiceImp implements RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImp(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private static final long REFRESH_TOKEN_DURATION = 3L * 24 * 60 * 60; // 3 days


    // 1️⃣ Create a new refresh token
    public RefreshToken createRefreshToken(User user, String deviceInfo) {
        // Optionally remove existing token for this device
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken token =
                RefreshToken.create(user, deviceInfo, REFRESH_TOKEN_DURATION);


        return refreshTokenRepository.save(token);
    }

    // 2️⃣ Verify refresh token validity
    public RefreshToken verifyRefreshToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));

        if (token.isExpired() || token.getRevoked()) {
            refreshTokenRepository.delete(token); // revoke expired token
            throw new RefreshTokenException("Refresh token expired or revoked");
        }

        return token;
    }

    // 3️⃣ Rotate refresh token (issue new, revoke old)
    public RefreshToken rotateToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return createRefreshToken(oldToken.getUser(), oldToken.getDeviceInfo());
    }

    // 4️⃣ Revoke single token (logout single device)
    public void revokeToken(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    // 5️⃣ Revoke all tokens for a user (logout all devices)
    public void revokeAllTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
        tokens.forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
