package com.vishal.todo.services.impl;

import com.vishal.todo.dto.TodoRequest;
import com.vishal.todo.dto.TodoResponse;
import com.vishal.todo.entity.Todo;
import com.vishal.todo.entity.User;
import com.vishal.todo.exception.ResourceNotFoundException;
import com.vishal.todo.repositories.TodoRepository;
import com.vishal.todo.repositories.UserRepository;
import com.vishal.todo.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TodoServiceImpl implements TodoService {
    @Autowired
    private final TodoRepository todoRepository;
    @Autowired
    private final UserRepository userRepository;

    public TodoServiceImpl(TodoRepository todoRepository,UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }
    @Override
    public Page<Todo> getTodos(Pageable pageable) {

        // 🔐 Get authenticated user safely
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return todoRepository.findByUserAndDeletedFalse(currentUser, pageable);
    }
    @Override
    public Todo createTodo(User user, TodoRequest request) {
        // Build the Todo entity
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        todo.setUser(user); // associate with current user

        // Save in DB
        return todoRepository.save(todo);
    }
    @Override
    public void deleteTodo(UUID todoId, UUID userId) {

        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Todo not found"));

        todoRepository.delete(todo);
    }
    @Override
    public TodoResponse updateTodo(
            UUID todoId,
            TodoResponse request,
            UUID userId
    ) {
        // 🔐 Fetch only user's todo
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Todo not found"));

        // ✏️ Update fields
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());

        Todo updated = todoRepository.save(todo);

        return mapToResponse(updated);
    }

    private TodoResponse mapToResponse(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
