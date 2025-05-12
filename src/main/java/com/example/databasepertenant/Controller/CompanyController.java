package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.dto.CompanyRegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public ResponseEntity<String> registerCompany(@RequestBody CompanyRegistrationDTO company) {
        String tenantId = company.getName().toLowerCase().replaceAll("\\s", "_");
        String dbName = "db_" + tenantId;

        tenantService.createTenant(tenantId, dbName);

        return ResponseEntity.ok("Company registered successfully with id: " + tenantId);
    }
}