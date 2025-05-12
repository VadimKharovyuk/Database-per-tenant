package com.example.databasepertenant.config;

import com.example.databasepertenant.DataSource.TenantAwareDataSource;
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

    public TenantInitializer(TenantRepository tenantRepository,
                             @Qualifier("tenantDataSource") DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            List<Tenant> tenants = tenantRepository.findAll();

            if (dataSource instanceof TenantAwareDataSource) {
                TenantAwareDataSource tenantAwareDataSource = (TenantAwareDataSource) dataSource;

                for (Tenant tenant : tenants) {
                    try {
                        HikariDataSource ds = new HikariDataSource();
                        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + tenant.getDbName());
                        ds.setUsername("postgres");
                        ds.setPassword("password");

                        tenantAwareDataSource.addTenant(tenant.getId(), ds);

                        System.out.println("Инициализирован тенант: " + tenant.getId() + " с БД: " + tenant.getDbName());
                    } catch (Exception e) {
                        System.err.println("Ошибка при инициализации тенанта " + tenant.getId() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации тенантов: " + e.getMessage());
            e.printStackTrace();
        }
    }
}