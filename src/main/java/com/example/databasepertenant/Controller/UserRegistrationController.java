package com.example.databasepertenant.Controller;



import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.Service.UserService;
import com.example.databasepertenant.dto.CreateUserDTO;
import com.example.databasepertenant.dto.UserResponseDTO;
import com.example.databasepertenant.model.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
@Slf4j
@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class UserRegistrationController {
    private final UserService userService;

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new CreateUserDTO());
        return "register";
    }

    @PostMapping
    public String registerUser(
            @ModelAttribute("user") CreateUserDTO userDTO,
            BindingResult bindingResult,
            Model model
    ) {
        try {
            // Базовая валидация
            if (bindingResult.hasErrors()) {
                return "register";
            }

            // Устанавливаем роль по умолчанию, если не указана
            if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                userDTO.setRoles(Set.of("ROLE_USER"));
            }

            // Регистрация пользователя
            UserResponseDTO registeredUser = userService.registerUser(userDTO);
            log.info(registeredUser.toString());

            // Перенаправление после успешной регистрации
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            // Обработка ошибок
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}