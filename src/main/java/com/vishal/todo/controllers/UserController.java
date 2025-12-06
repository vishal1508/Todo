package com.vishal.todo.controllers;

import com.vishal.todo.dto.UserDtoRequest;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Create User
    @PutMapping
    public ResponseEntity<?> createUser(@RequestBody UserDtoRequest userDtoRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                System.out.println("Authentication failed");
            }
            String email = authentication.getName();
            User user = userRepository.findByEmail(userDtoRequest.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
            userService.createUser(userDtoRequest);
            return new ResponseEntity<>(SecurityContextHolder.getContext().getAuthentication().getName(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

}
