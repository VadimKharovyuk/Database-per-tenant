package com.example.databasepertenant.dto;



import com.example.databasepertenant.model.User;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CreateUserDTO {
    private User user;
    private Set<String> roles = new HashSet<>();
}
