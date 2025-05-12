package com.example.databasepertenant.Controller;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.Service.UserService;
import com.example.databasepertenant.dto.CreateUserDTO;
import com.example.databasepertenant.model.Role;
import com.example.databasepertenant.model.User;
import com.example.databasepertenant.repository.RoleRepository;
import com.example.databasepertenant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private  final  UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user, @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        try {
            // Выводим информацию для отладки
            System.out.println("Получен запрос на создание пользователя");
            System.out.println("Заголовок X-Tenant-ID: " + tenantId);
            System.out.println("Текущий тенант в контексте: " + TenantContext.getTenantId());

            // Установим контекст тенанта вручную, если нужно
            if (tenantId != null && !tenantId.equals(TenantContext.getTenantId())) {
                TenantContext.setTenantId(tenantId);
                System.out.println("Тенант установлен вручную: " + tenantId);
            }

            // Упрощенное создание пользователя без указания ролей
            user.setRoles(new HashSet<>()); // Пустой набор ролей
            User savedUser = userRepository.save(user);

            return ResponseEntity.ok("Пользователь успешно создан с ID: " + savedUser.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.getUserById(id)
                .map(existingUser -> {
                    user.setId(id);
                    return ResponseEntity.ok(userService.updateUser(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    userService.deleteUser(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabaseConnection(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        try {
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }

            System.out.println("Текущий тенант: " + TenantContext.getTenantId());

            // Проверим подключение, выполнив простой запрос
            long userCount = userRepository.count();

            return ResponseEntity.ok("Успешное подключение к БД. Количество пользователей: " + userCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка при подключении к БД: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}
