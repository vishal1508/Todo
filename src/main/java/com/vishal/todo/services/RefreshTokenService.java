package com.vishal.todo.services;

import com.vishal.todo.entity.User;
import com.vishal.todo.security.RefreshToken;
import com.vishal.todo.utils.LoginRequest;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user, String deviceInfo);

    RefreshToken verifyRefreshToken(String tokenStr);

    RefreshToken rotateToken(RefreshToken oldToken);

    void revokeToken(String tokenStr);

    void revokeAllTokens(User user);
}
