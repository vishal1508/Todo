package com.vishal.todo.services.impl;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.entity.Role;
import com.vishal.todo.entity.User;
import com.vishal.todo.enums.RoleEnum;
import com.vishal.todo.repositories.RoleRepository;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.services.UserOtpManager;
import com.vishal.todo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder; // injected automatically
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserOtpManager userOtpManager;
    public void createUser(UserDtoRequest request) {
        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Invalid email format");
        }

        Set<Role> roles = new HashSet<>();
        for (RoleEnum roleData : request.getRoles()) {
            Role role = roleRepository.findByName(roleData)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleData));
            roles.add(role);
        }

        user.setRoles(roles);
        // Hash password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
         userRepository.save(user);

         userOtpManager.sendOtpToUser(request.getEmail());
    }
    /**
     * Verify OTP and activate user
     */
    public boolean verifyEmailOtp(String email, String otp) {
        // 1. Check OTP from Redis
        boolean isValid = userOtpManager.verifyOtp(email, otp);

        if (!isValid) {
            return false; // OTP invalid or expired
        }

        // 2. Update user in DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.set_email_verified(true);
        userRepository.save(user);
        return true;
    }
}
