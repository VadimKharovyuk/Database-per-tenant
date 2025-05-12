package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.LoginRequestDTO;
import com.example.databasepertenant.dto.LoginResponseDTO;
import com.example.databasepertenant.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO loginRequest, String tenantId) {
        try {
            // Устанавливаем контекст тенанта
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
                System.out.println("Установлен контекст тенанта для авторизации пользователя: " + tenantId);
            }

            // Находим пользователя по имени
            Optional<User> userOptional = userService.getUserByUsername(loginRequest.getUsername());

            if (userOptional.isEmpty()) {
                return new LoginResponseDTO(null, null, "Пользователь не найден", false);
            }

            User user = userOptional.get();

            // Проверяем пароль
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                return new LoginResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        "Авторизация успешна",
                        true
                );
            } else {
                return new LoginResponseDTO(null, null, "Неверный пароль", false);
            }
        } finally {
            // Очищаем контекст тенанта
            TenantContext.clear();
        }
    }
}
