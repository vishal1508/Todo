package com.vishal.todo.services;

import com.vishal.todo.dto.UserDtoRequest;
import com.vishal.todo.entity.User;

public interface UserService  {
    User createUser(UserDtoRequest request);
}
