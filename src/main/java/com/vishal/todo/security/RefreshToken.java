package com.vishal.todo.security;


import com.vishal.todo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false, length = 128)
    private String token; // Random UUID token

    @Column(nullable = false)
    private Instant expiryDate; // Expiration timestamp

    @Column(nullable = true, length = 150)
    private String deviceInfo; // Optional: device/browser info

    @Column(nullable = false)
    private Boolean revoked = false; // Support revocation

    // Constructors
    public RefreshToken() {}

    // Utility factory method
    public static RefreshToken create(User user, String deviceInfo, long durationSeconds) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setDeviceInfo(deviceInfo);
        token.setExpiryDate(Instant.now().plusSeconds(durationSeconds));
        token.setRevoked(false);
        return token;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

}
