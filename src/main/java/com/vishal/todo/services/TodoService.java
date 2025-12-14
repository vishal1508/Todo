package com.vishal.todo.services;

import com.vishal.todo.dto.TodoRequest;
import com.vishal.todo.dto.TodoResponse;
import com.vishal.todo.entity.Todo;
import com.vishal.todo.entity.User;
import com.vishal.todo.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TodoService {
    Page<Todo> getTodos(Pageable pageable);

    Todo createTodo(User user, TodoRequest request);

    void deleteTodo(UUID todoId, UUID userId);

    TodoResponse updateTodo(
            UUID todoId,
            TodoResponse request,
            UUID userId
    );
}
