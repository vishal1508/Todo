package com.vishal.todo.services;

public interface UserOtpManager {
    void sendOtpToUser(String email);
    boolean verifyOtp(String email, String otp);
}
