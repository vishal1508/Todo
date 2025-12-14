package com.vishal.todo.dto;

import com.vishal.todo.entity.Role;
import com.vishal.todo.enums.RoleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
public class UserDtoRequest {
    @NotBlank(message = "Name cannot be empty")
    @NotNull(message = "Name Cannot be Null")
    private String name;
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email Cannot be Null")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @NotNull(message = "Password Cannot be Null")
    private String password;

    private Set<RoleEnum> roles = new HashSet<>();

}
