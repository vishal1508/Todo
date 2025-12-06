package com.vishal.todo.repositories;

import com.vishal.todo.entity.Role;
import com.vishal.todo.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    // Find role by name → ROLE_ADMIN, ROLE_USER
    Optional <Role> findByName(RoleEnum name);

    // Check if role exists
    boolean existsByName(RoleEnum name);
}
