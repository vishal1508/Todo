package com.vishal.todo.services;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.entity.User;

public interface UserService  {
    void createUser(UserDtoRequest request);
    boolean verifyEmailOtp(String email, String otp);
}
