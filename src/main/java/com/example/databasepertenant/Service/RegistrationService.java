package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.CreateUserDTO;
import com.example.databasepertenant.dto.UserResponseDTO;

import com.example.databasepertenant.maper.UserMapper;
import com.example.databasepertenant.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerUser(CreateUserDTO userDTO, String tenantId) {
        try {
            // Устанавливаем контекст тенанта
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
                System.out.println("Установлен контекст тенанта для регистрации пользователя: " + tenantId);
            }

            // Проверяем, существует ли пользователь с таким именем
            if (userService.getUserByUsername(userDTO.getUsername()).isPresent()) {
                throw new RuntimeException("Пользователь с таким именем уже существует");
            }

            // Преобразуем DTO в модель User
            User user = userMapper.toEntity(userDTO);

            // Шифруем пароль
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));

            // Создаем пользователя с указанными ролями
            User savedUser = userService.createUser(user, userDTO.getRoles());

            // Возвращаем DTO ответа
            return userMapper.toResponseDto(savedUser);
        } finally {
            // Очищаем контекст тенанта
            TenantContext.clear();
        }
    }

    public boolean isUsernameAvailable(String username) {
        return userService.getUserByUsername(username).isPresent();
    }

    public boolean isEmailAvailable(String email) {
        return userService.getUserByUsername(email).isPresent();
    }
}