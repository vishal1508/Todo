package com.vishal.todo.controllers;

import com.vishal.todo.dto.ApiResponse;
import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.dto.VerifyOtpRequest;
import com.vishal.todo.entity.User;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.services.RedisService;
import com.vishal.todo.services.UserOtpManager;
import com.vishal.todo.services.UserService;
import com.vishal.todo.services.impl.UserDetailServiceImpl;
import com.vishal.todo.security.JwtUtil;
import com.vishal.todo.utils.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailServiceImpl userDetailServiceImpl;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    // Create

    @Autowired
    private RedisService redisService;

    @GetMapping("/health/redis")
    public ResponseEntity<?> checkRedisHealth() {

        boolean redisOK = redisService.isRedisHealthy();

        Map<String, Object> response = new HashMap<>();
        response.put("redis_status", redisOK ? "UP" : "DOWN");

        if (redisOK) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDtoRequest user) {
        try {
            userService.createUser(user);
            ApiResponse otpResponse = new ApiResponse(true, "An OTP has been sent to your email. Please check your inbox.", user.getEmail());

            return new ResponseEntity<>(otpResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserOtpManager userOtpManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest user) {
        try {
            // 1. Authenticate username + password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword()
                    )
            );
            User userData = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            if (!userData.is_email_verified()) {
                userOtpManager.sendOtpToUser(userData.getEmail());
                ApiResponse apiResponse = new ApiResponse(true, "An OTP has been sent to your email. Please check your inbox.", userData.getEmail());
                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
            }
            // 2. Load user details
            UserDetails userDetails =
                    userDetailServiceImpl.loadUserByUsername(user.getEmail());

            // 3. Generate JWT token
            String token = jwtUtil.generateToken(userDetails.getUsername());

            // 4. Return JWT in response
            return ResponseEntity.ok(token);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyOtpRequest request) {
        boolean verified = userService.verifyEmailOtp(request.getEmail(), request.getOtp());

        if (!verified) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid or expired OTP", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Email verified successfully", null));
    }
}
