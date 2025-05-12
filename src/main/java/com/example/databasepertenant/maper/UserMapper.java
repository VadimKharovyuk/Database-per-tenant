package com.example.databasepertenant.maper;
import com.example.databasepertenant.dto.CreateUserDTO;

import com.example.databasepertenant.dto.UserResponseDTO;
import com.example.databasepertenant.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Преобразует объект CreateUserDTO в объект User
     */
    public User toEntity(CreateUserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword()); // Пароль будет зашифрован в сервисе
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setIsActive(true);
        return user;
    }

    public UserResponseDTO toResponseDto(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}