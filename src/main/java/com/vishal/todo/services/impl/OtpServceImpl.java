package com.vishal.todo.services.impl;

import com.vishal.todo.services.OtpService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpServceImpl implements OtpService {
    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        int otp = random.nextInt(900000) + 100000; // 6-digit
        return String.valueOf(otp);
    }
}


