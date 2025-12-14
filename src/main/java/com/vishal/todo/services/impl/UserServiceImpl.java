package com.vishal.todo.services.impl;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.dto.UserResponse;
import com.vishal.todo.entity.Role;
import com.vishal.todo.entity.User;
import com.vishal.todo.enums.RoleEnum;
import com.vishal.todo.exception.ResourceNotFoundException;
import com.vishal.todo.exception.UserAlreadyExistsException;
import com.vishal.todo.repositories.RoleRepository;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.services.UserOtpManager;
import com.vishal.todo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new RuntimeException("Invalid email format");
        }

        Set<Role> roles = new HashSet<>();
        RoleEnum userRole = RoleEnum.valueOf("USER");

        Role role = roleRepository.findByName(userRole)
                .orElseThrow(() -> new RuntimeException("Role not found: " + userRole));
        roles.add(role);



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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public UserResponse getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
    @Override
    public long getTotalUsers() {
        return userRepository.countByDeletedFalse();
        // OR: return userRepository.count();
    }
}
