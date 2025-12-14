package com.vishal.todo.controllers;

import com.vishal.todo.dto.ApiResponse;
import com.vishal.todo.dto.AuthResponse;
import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.dto.VerifyOtpRequest;
import com.vishal.todo.entity.Role;
import com.vishal.todo.entity.User;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.security.RefreshToken;
import com.vishal.todo.services.RedisService;
import com.vishal.todo.services.RefreshTokenService;
import com.vishal.todo.services.UserOtpManager;
import com.vishal.todo.services.UserService;
import com.vishal.todo.services.impl.UserDetailServiceImpl;
import com.vishal.todo.security.JwtUtil;
import com.vishal.todo.utils.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailServiceImpl userDetailServiceImpl;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;
    // Create

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailServiceImpl userDetailServiceImpl) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailServiceImpl = userDetailServiceImpl;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDtoRequest user, BindingResult result) {
        try {
            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body(result.getFieldError().getDefaultMessage());
            }
            userService.createUser(user);
            ApiResponse otpResponse = new ApiResponse(true, "User has been Created", user.getEmail());

            return new ResponseEntity<>(otpResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse otpResponse = new ApiResponse(true, e.getMessage(), user.getEmail());
            return new ResponseEntity<>(otpResponse, HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest user, HttpServletResponse response,
                                   HttpServletRequest httpRequest) {
        try {
            // 1. Authenticate username + password
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            User userData = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
//            if (!userData.is_email_verified()) {
//                userOtpManager.sendOtpToUser(userData.getEmail());
//                ApiResponse apiResponse = new ApiResponse(true, "An OTP has been sent to your email. Please check your inbox.", userData.getEmail());
//                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//            }
            // 2. Load user details
            UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(user.getEmail());

            String[] rolesArray = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);

            // 3. Generate JWT token
            String accessToken = jwtUtil.generateToken(userDetails.getUsername(),rolesArray);

            // 3️⃣ Generate REFRESH token (long-lived)
            String deviceInfo = httpRequest.getHeader("User-Agent");
            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(
                            userData,
                            deviceInfo
                    );
            // 4️⃣ Store refresh token in HttpOnly cookie
            String toke = refreshToken.getToken();
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(false)          // HTTPS only
                    .sameSite("Lax")    // CSRF protection
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 5️⃣ Return ACCESS token in response body

            return ResponseEntity.ok(new AuthResponse(accessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenValue
    ) {

        if (refreshTokenValue != null) {
            refreshTokenService.revokeToken(refreshTokenValue);
        }

        // Clear cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)   // true in prod
                .sameSite("Lax")
                .path("/")
                .maxAge(0)       // 🔥 delete cookie
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new ApiResponse(true, "Logged out successfully", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenValue
    ) {

        if (refreshTokenValue == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        // 1️⃣ Validate refresh token
        RefreshToken oldToken =
                refreshTokenService.verifyRefreshToken(refreshTokenValue);

        User user = oldToken.getUser();
        String[] rolesArray = user.getRoles().stream()
                .map(Role::getName) // Assuming Role has a getName() method
                .toArray(String[]::new);                // 3. Join them

        // 2️⃣ Generate new access token
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), rolesArray);

        // 3️⃣ Rotate refresh token (BEST PRACTICE)
        RefreshToken newRefreshToken =
                refreshTokenService.rotateToken(oldToken);

        // 4️⃣ Set new refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken.getToken())
                .httpOnly(true)
                .secure(false) // true in prod
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(newAccessToken));
    }

//    @PostMapping("/verify-email")
//    public ResponseEntity<?> verifyEmail(@RequestBody VerifyOtpRequest request) {
//        boolean verified = userService.verifyEmailOtp(request.getEmail(), request.getOtp());
//
//        if (!verified) {
//            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid or expired OTP", null));
//        }
//
//        return ResponseEntity.ok(new ApiResponse<>(true, "Email verified successfully", null));
//    }
}
