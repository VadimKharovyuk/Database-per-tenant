package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.dto.TenantDTO;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.FlightRepository;
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




    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

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


    @GetMapping("/{id}")
    public ResponseEntity<?> getTenantById(@PathVariable String id) {
        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/init-admin")
    public ResponseEntity<?> initializeAdminDb() {
        try {
            tenantService.initializeAdminDatabase();
            return ResponseEntity.ok(Map.of(
                    "message", "База данных администратора успешно инициализирована"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Ошибка при инициализации базы данных: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/reinitialize")
    public ResponseEntity<?> reinitializeExistingTenants() {
        try {
            tenantService.reinitializeExistingTenants();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All companies successfully reinitialized"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error reinitializing companies: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh-repositories")
    public ResponseEntity<?> refreshRepositories() {
        try {
            // Получаем список всех активных тенантов
            List<Tenant> tenants = tenantService.getAllTenants();

            // Очищаем текущую карту репозиториев (внимание: используем правильную карту)
            Map<String, FlightRepository> flightRepositories = tenantService.getFlightRepositories();
            flightRepositories.clear();

            // Для каждого тенанта создаем новый репозиторий
            for (Tenant tenant : tenants) {
                String tenantId = tenant.getId();
                try {
                    // Переинициализируем тенанта
                    tenantService.initializeTenant(tenant);
                    System.out.println("Репозиторий для тенанта " + tenantId + " успешно создан");
                } catch (Exception e) {
                    System.err.println("Ошибка при создании репозитория для тенанта " + tenantId + ": " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Repositories refreshed successfully",
                    "count", flightRepositories.size(),
                    "repositories", flightRepositories.keySet()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error refreshing repositories: " + e.getMessage()
            ));
        }
    }

}