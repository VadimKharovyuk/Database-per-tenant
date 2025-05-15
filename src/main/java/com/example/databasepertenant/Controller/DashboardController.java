package com.example.databasepertenant.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        model.addAttribute("username", userDetails.getUsername());
        return "dashboard";
    }
}
