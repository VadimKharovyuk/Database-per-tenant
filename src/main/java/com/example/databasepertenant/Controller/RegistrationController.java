package com.example.databasepertenant.Controller;
import com.example.databasepertenant.Service.RegistrationService;
import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.CreateUserDTO;
import com.example.databasepertenant.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<?> registerUser(
            @RequestBody CreateUserDTO userDTO,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            // Установка контекста тенанта
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
                System.out.println("Установлен контекст тенанта для регистрации: " + tenantId);
            }

            // Регистрация пользователя
            UserResponseDTO registeredUser = registrationService.registerUser(userDTO, tenantId);

            // Подготовка ответа
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Пользователь успешно зарегистрирован");
            response.put("user", registeredUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при регистрации пользователя: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        } finally {
            // Очистка контекста тенанта
            TenantContext.clear();
        }
    }

    @PostMapping("/validate-username")
    public ResponseEntity<?> validateUsername(
            @RequestBody Map<String, String> usernameData,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            // Установка контекста тенанта
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
            }

            String username = usernameData.get("username");
            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Имя пользователя не может быть пустым"
                ));
            }

            boolean isAvailable = registrationService.isUsernameAvailable(username);

            return ResponseEntity.ok(Map.of(
                    "valid", isAvailable,
                    "message", isAvailable ? "Имя пользователя доступно" : "Имя пользователя уже занято"
            ));
        } finally {
            // Очистка контекста тенанта
            TenantContext.clear();
        }
    }

    @PostMapping("/validate-email")
    public ResponseEntity<?> validateEmail(
            @RequestBody Map<String, String> emailData,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            // Установка контекста тенанта
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
            }

            String email = emailData.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Email не может быть пустым"
                ));
            }

            boolean isAvailable = registrationService.isEmailAvailable(email);

            return ResponseEntity.ok(Map.of(
                    "valid", isAvailable,
                    "message", isAvailable ? "Email доступен" : "Email уже занят"
            ));
        } finally {
            // Очистка контекста тенанта
            TenantContext.clear();
        }
    }
}