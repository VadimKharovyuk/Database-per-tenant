package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.RegistrationService;
import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.CreateUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
                            Model model) {
        if (tenantId != null && !tenantId.isEmpty()) {
            model.addAttribute("tenantId", tenantId);
        }

        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль!");
        }

        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы!");
        }

        return "login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
                               Model model) {
        if (tenantId != null && !tenantId.isEmpty()) {
            model.addAttribute("tenantId", tenantId);
        }

        model.addAttribute("user", new CreateUserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") CreateUserDTO userDTO,
                               @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        try {
            registrationService.registerUser(userDTO, tenantId);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            return "redirect:/register?error=true";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
                            Model model) {
        if (tenantId != null && !tenantId.isEmpty()) {
            TenantContext.setTenantId(tenantId);
            model.addAttribute("tenantId", tenantId);
        }

        return "dashboard";
    }

    @GetMapping()
    public String index(Model model) {
        return "home";
    }
}