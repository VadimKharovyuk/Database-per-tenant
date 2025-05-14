package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.FlightServiceData;
import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller()
public class HomeController {

    private final FlightServiceData flightServiceData;

    private final TenantService tenantService;

    @GetMapping()
    public String index(Model model) {

        List<Tenant> tenants = tenantService.getAllTenants();
        model.addAttribute("tenants", tenants);


        return "home";
    }
}
