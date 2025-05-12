package com.example.databasepertenant.Controller;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.Service.UserService;
import com.example.databasepertenant.dto.UserResponseDTO;
import com.example.databasepertenant.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            // Устанавливаем контекст тенанта, если он указан в заголовке
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
                System.out.println("Установлен контекст тенанта для получения всех пользователей: " + tenantId);
            }

            // Получаем пользователей через сервис
            List<UserResponseDTO> users = userService.getAllUsersDto();

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        } finally {
            // Важно очистить контекст тенанта после выполнения запроса
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
            }

            return userService.getUserDtoById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            TenantContext.clear();
        }
    }

//    @GetMapping("/by-username/{username}")
//    public ResponseEntity<UserResponseDTO> getUserByUsername(
//            @PathVariable String username,
//            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
//    ) {
//        try {
//            if (tenantId != null && !tenantId.isEmpty()) {
//                TenantContext.setTenantId(tenantId);
//            }
//
//            return userService.getUserByUsername(username)
//                    .map(user -> ResponseEntity.ok(userService.getUserMapper().toResponseDto(user)))
//                    .orElse(ResponseEntity.notFound().build());
//        } finally {
//            TenantContext.clear();
//        }
//    }

//    @PutMapping("/{id}")
//    public ResponseEntity<UserResponseDTO> updateUser(
//            @PathVariable Long id,
//            @RequestBody User userUpdate,
//            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
//    ) {
//        try {
//            if (tenantId != null && !tenantId.isEmpty()) {
//                TenantContext.setTenantId(tenantId);
//            }
//
//            return userService.getUserById(id)
//                    .map(existingUser -> {
//                        // Обновляем только нужные поля
//                        if (userUpdate.getFirstName() != null) {
//                            existingUser.setFirstName(userUpdate.getFirstName());
//                        }
//                        if (userUpdate.getLastName() != null) {
//                            existingUser.setLastName(userUpdate.getLastName());
//                        }
//                        if (userUpdate.getEmail() != null) {
//                            existingUser.setEmail(userUpdate.getEmail());
//                        }
//                        if (userUpdate.getIsActive() != null) {
//                            existingUser.setIsActive(userUpdate.getIsActive());
//                        }
//
//                        // Не обновляем пароль через этот метод
//
//                        User updatedUser = userService.updateUser(existingUser);
//                        return ResponseEntity.ok(userService.getUserMapper().toResponseDto(updatedUser));
//                    })
//                    .orElse(ResponseEntity.notFound().build());
//        } finally {
//            TenantContext.clear();
//        }
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
            }

            return userService.getUserById(id)
                    .map(user -> {
                        userService.deleteUser(id);
                        return ResponseEntity.ok(Map.of("message", "Пользователь успешно удален"));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabaseConnection(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }

            System.out.println("Текущий тенант: " + TenantContext.getTenantId());

            // Проверим подключение, выполнив простой запрос
            long userCount = userService.getAllUsers().size();

            return ResponseEntity.ok("Успешное подключение к БД. Количество пользователей: " + userCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка при подключении к БД: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
            }

            String newPassword = passwordData.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Новый пароль не может быть пустым"));
            }

            return userService.getUserById(id)
                    .map(user -> {
                        // Устанавливаем новый зашифрованный пароль
                        user.setPasswordHash(userService.getPasswordEncoder().encode(newPassword));
                        userService.updateUser(user);
                        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            TenantContext.clear();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> changeUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusData,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setTenantId(tenantId);
            }

            Boolean isActive = statusData.get("isActive");
            if (isActive == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Статус активности не указан"));
            }

            return userService.getUserById(id)
                    .map(user -> {
                        user.setIsActive(isActive);
                        userService.updateUser(user);
                        String message = isActive ? "Пользователь активирован" : "Пользователь деактивирован";
                        return ResponseEntity.ok(Map.of("message", message));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            TenantContext.clear();
        }
    }
}