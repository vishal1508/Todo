package com.vishal.todo.controllers;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.entity.User;
import com.vishal.todo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private  UserService userService;

    // Create User
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDtoRequest user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(savedUser);
    }
}
