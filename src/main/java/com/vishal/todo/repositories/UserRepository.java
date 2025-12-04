package com.vishal.todo.repositories;

import com.vishal.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if email already exists (useful for signup)
    boolean existsByEmail(String email);
}
