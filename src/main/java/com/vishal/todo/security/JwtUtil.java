package com.vishal.todo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;


@Component
public class JwtUtil {
    // ⏳ 24 hours expiration
    @Value("${jwt.access-token-expiration-ms}")
    private long EXPIRATION_TIME;
    // 🔑 Secret key (at least 256-bit)
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private Key getSigningKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    // =========================
    // 🔹 Generate JWT Token
    // =========================
    public String generateToken(String email) {

        return Jwts.builder()
                .setSubject(email)                  // username / email
                .setIssuedAt(new Date())               // token issue time
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    // =========================
    // 🔹 Extract Username
    // =========================
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    // =========================
    // 🔹 Validate Token
    // =========================
    // Validate token
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    // =========================
    // 🔹 Check if Expired
    // =========================
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
    // =========================
    // 🔹 Extract Claims
    // =========================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
