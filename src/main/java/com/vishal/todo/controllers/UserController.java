package com.vishal.todo.controllers;

import com.vishal.todo.dto.ApiResponse;
import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.dto.UserResponse;
import com.vishal.todo.entity.User;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile() {

        UserResponse user = userService.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse(true, "User profile fetched", user)
        );
    }
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> getTotalUsers() {
        long count = userService.getTotalUsers();

        return ResponseEntity.ok(
                new ApiResponse(true, "Total users fetched successfully", count)
        );
    }
}
