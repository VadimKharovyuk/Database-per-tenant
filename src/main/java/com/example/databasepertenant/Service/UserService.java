package com.example.databasepertenant.Service;

import com.example.databasepertenant.dto.CreateUserDTO;
import com.example.databasepertenant.dto.UserResponseDTO;

import com.example.databasepertenant.maper.UserMapper;
import com.example.databasepertenant.model.Role;
import com.example.databasepertenant.model.User;
import com.example.databasepertenant.repository.RoleRepository;
import com.example.databasepertenant.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDTO registerUser(CreateUserDTO userDTO) {
        // Логирование входящих данных
        log.info("Регистрация пользователя: {}", userDTO.getUsername());

        // Проверка уникальности username
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            log.error("Попытка регистрации с существующим username: {}", userDTO.getUsername());
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        // Проверка уникальности email
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            log.error("Попытка регистрации с существующим email: {}", userDTO.getEmail());
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Преобразуем DTO в модель пользователя
        User user = userMapper.toEntity(userDTO);

        // Шифруем пароль
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));

        // Устанавливаем роли
        Set<String> roleNames = userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()
                ? userDTO.getRoles()
                : Set.of("ROLE_USER");

        // Получаем роли из базы данных с обработкой ошибок
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        log.error("Роль не найдена: {}", roleName);
                        return new RuntimeException("Роль не найдена: " + roleName);
                    });
            roles.add(role);
        }

        // Назначаем роли пользователю
        user.setRoles(roles);

        try {
            // Сохраняем пользователя
            User savedUser = userRepository.save(user);
            log.info("Пользователь зарегистрирован успешно: {}", savedUser.getUsername());

            // Возвращаем DTO сохраненного пользователя
            return userMapper.toResponseDto(savedUser);
        } catch (Exception e) {
            log.error("Ошибка при сохранении пользователя: ", e);
            throw new RuntimeException("Не удалось сохранить пользователя", e);
        }
    }

    // Другие методы без изменений
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}