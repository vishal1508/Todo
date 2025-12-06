package com.vishal.todo.services.impl;

import com.vishal.todo.services.EmailService;
import com.vishal.todo.services.OtpService;
import com.vishal.todo.services.RedisService;
import com.vishal.todo.services.UserOtpManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UserOtpManagerImpl implements UserOtpManager {

    @Autowired
    private OtpService otpService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;

    @Async
    public void sendOtpToUser(String email) {
        String otp = otpService.generateOtp();
        redisService.setValue("email_verify_otp:" + email, otp, 300);
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = redisService.getValue("email_verify_otp:" + email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return false;
        }
        redisService.deleteKey("email_verify_otp:" + email);
        return true;
    }


}
