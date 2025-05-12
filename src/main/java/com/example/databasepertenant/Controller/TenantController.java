package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.dto.TenantDTO;
import com.example.databasepertenant.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<?> createTenant(@RequestBody TenantDTO tenantDTO) {
        try {
            Tenant tenant = tenantService.createTenant(
                    tenantDTO.getId(),
                    "db_" + tenantDTO.getId(),
                    tenantDTO.getDescription()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Company registered successfully with id: " + tenant.getId(),
                    "tenant", tenant
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Error creating tenant: " + e.getMessage()
            ));
        }
    }


    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTenantById(@PathVariable String id) {
        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}