package com.vishal.todo.services.impl;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.entity.User;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // injected automatically

    public User createUser(UserDtoRequest request) {
        // Check duplicate email

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getEmail()  != null  && request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) { // Check Email Validation
            throw new RuntimeException("Invalid email format");
        }

        // Hash password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}
