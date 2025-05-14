package com.example.databasepertenant.DataSource;

import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.TenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.List;

@Component
@DependsOn("adminFlyway")
public class TenantInitializer implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;
    private final TenantService tenantService;

    public TenantInitializer(TenantRepository tenantRepository,
                             @Qualifier("tenantDataSource") DataSource dataSource , TenantService tenantService) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
        this.tenantService =tenantService ;
    }

//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        try {
//            List<Tenant> tenants = tenantRepository.findAll();
//
//            if (dataSource instanceof TenantAwareDataSource) {
//                TenantAwareDataSource tenantAwareDataSource = (TenantAwareDataSource) dataSource;
//
//                for (Tenant tenant : tenants) {
//                    try {
//                        HikariDataSource ds = new HikariDataSource();
//                        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + tenant.getDbName());
//                        ds.setUsername("postgres");
//                        ds.setPassword("password");
//
//                        tenantAwareDataSource.addTenant(tenant.getId(), ds);
//
//                        System.out.println("Инициализирован тенант: " + tenant.getId() + " с БД: " + tenant.getDbName());
//                    } catch (Exception e) {
//                        System.err.println("Ошибка при инициализации тенанта " + tenant.getId() + ": " + e.getMessage());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Ошибка при инициализации тенантов: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Получаем список всех тенантов из репозитория
            List<Tenant> tenants = tenantRepository.findAll();

            // Проверяем, является ли DataSource экземпляром TenantAwareDataSource
            if (dataSource instanceof TenantAwareDataSource) {
                TenantAwareDataSource tenantAwareDataSource = (TenantAwareDataSource) dataSource;

                // Перебираем всех тенантов
                for (Tenant tenant : tenants) {
                    try {
                        // Инициализируем каждого тенанта
                        tenantService.initializeTenant(tenant);
                        System.out.println("Инициализирован тенант: " + tenant.getId() + " с БД: " + tenant.getDbName());
                    } catch (Exception e) {
                        // Обработка ошибок для каждого отдельного тенанта
                        System.err.println("Ошибка при инициализации тенанта " + tenant.getId() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Обработка общих ошибок при инициализации тенантов
            System.err.println("Ошибка при инициализации тенантов: " + e.getMessage());
            e.printStackTrace();
        }
    }

}