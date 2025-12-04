package com.vishal.todo.controllers;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.entity.User;
import com.vishal.todo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private UserService userService;

    // Create User
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDtoRequest user) {
        try{
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(savedUser);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}
