package com.vishal.todo.controllers;

import com.vishal.todo.dto.ApiResponse;
import com.vishal.todo.dto.TodoRequest;
import com.vishal.todo.dto.TodoResponse;
import com.vishal.todo.entity.Todo;
import com.vishal.todo.entity.User;
import com.vishal.todo.services.TodoService;
import com.vishal.todo.services.UserService;
import com.vishal.todo.services.impl.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/todo")
public class TodoController {

    @Autowired
    private final TodoService todoService;
    @Autowired
    private final UserService userService;

    public TodoController(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }


    /**
     * Get paginated todos of the logged-in user
     */
    @GetMapping
    public ResponseEntity<?> getTodos(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<Todo> todos = todoService.getTodos(pageable);
        // Convert to DTO
        // Convert Page<Todo> → Page<TodoResponse>
        Page<TodoResponse> responsePage = todos.map(todo ->
                new TodoResponse(
                        todo.getId(),
                        todo.getTitle(),
                        todo.getDescription(),
                        todo.getCreatedAt(),
                        todo.getUpdatedAt()
                )
        );

        return ResponseEntity.ok(
                new ApiResponse(true, "Todos fetched successfully", responsePage)
        );    }

    // -----------------------------
    // CREATE TODO
    // -----------------------------
    @PostMapping
    public ResponseEntity<ApiResponse> createTodo(
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails // automatically injected by Spring Security
    ) {
        try {
            // Get the logged-in user
            String email = customUserDetails.getUsername();
            User user = userService.findByEmail(email); // return User entity from DB
            // Create todo
            Todo todo = todoService.createTodo(user, request);

            // Convert to DTO
            TodoResponse response = new TodoResponse(
                    todo.getId(),
                    todo.getTitle(),
                    todo.getDescription(),
                    todo.getCreatedAt(),
                    todo.getUpdatedAt()
            );
            return new ResponseEntity<>(
                    new ApiResponse(true, "Todo created successfully", response),
                    HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "Failed to create todo: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @PutMapping("/{todoId}")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @PathVariable UUID todoId,
            @Valid @RequestBody TodoResponse request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        TodoResponse updatedTodo =
                todoService.updateTodo(todoId, request, user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Todo updated successfully", updatedTodo)
        );
    }

    // 🔐 DELETE TODO (Only owner can delete)
    @DeleteMapping("/{todoId}")
    public ResponseEntity<ApiResponse> deleteTodo(
            @PathVariable UUID todoId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        todoService.deleteTodo(todoId, user.getId());

        return ResponseEntity.ok(
                new ApiResponse(true, "Todo deleted successfully", null)
        );
    }

}
