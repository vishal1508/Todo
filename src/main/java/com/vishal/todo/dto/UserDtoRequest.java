package com.vishal.todo.dto;

import com.vishal.todo.entity.Role;
import com.vishal.todo.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
public class UserDtoRequest {
    private String name;
    private String email;
    private String password;

    private Set<RoleEnum> roles = new HashSet<>();

}
