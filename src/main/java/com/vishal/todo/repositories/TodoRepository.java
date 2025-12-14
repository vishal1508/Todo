package com.vishal.todo.repositories;

import com.vishal.todo.entity.Todo;
import com.vishal.todo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
    // 🔹 Get all todos for a user (not deleted)
    Page<Todo> findByUserAndDeletedFalse(User user, Pageable pageable);
    Optional<Todo> findByIdAndUserId(UUID todoId, UUID userId);

    // 🔹 Get single todo by id and user (ownership check)
    Optional<Todo> findByIdAndUserAndDeletedFalse(UUID id, User user);

    // 🔹 Check ownership (useful for update/delete)
    boolean existsByIdAndUserAndDeletedFalse(UUID id, User user);
}
