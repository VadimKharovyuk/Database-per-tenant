package com.example.databasepertenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    private String username;
    private String email;

    private String password;

    private String firstName;
    private String lastName;

    private Set<String> roles;
}