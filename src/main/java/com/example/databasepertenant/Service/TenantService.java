package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantAwareDataSource;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.TenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final TenantRepository tenantRepository;

    public TenantService(@Qualifier("tenantDataSource") DataSource dataSource,
                         JdbcTemplate jdbcTemplate,
                         TenantRepository tenantRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantRepository = tenantRepository;
    }


    @Transactional
    public Tenant createTenant(String tenantId, String dbName, String description) {
        // Проверяем, существует ли тенант
        if (tenantRepository.findById(tenantId).isPresent()) {
            throw new RuntimeException("Тенант с ID " + tenantId + " уже существует");
        }

        try {
            // Создаем новую БД
            jdbcTemplate.execute("CREATE DATABASE " + dbName);
            System.out.println("Создание тенанта с ID: [" + tenantId + "] и базой данных: [" + dbName + "]");

            // Создаем DataSource для новой БД
            HikariDataSource newDataSource = createDataSource(dbName);

            // Добавляем DataSource для нового клиента
            if (dataSource instanceof TenantAwareDataSource) {
                ((TenantAwareDataSource) dataSource).addTenant(tenantId, newDataSource);
            }

            // Выполняем миграцию Flyway
            runFlywayMigration(newDataSource);

            // Сохраняем информацию о тенанте
            Tenant tenant = new Tenant();
            tenant.setId(tenantId);
            tenant.setDbName(dbName);
            tenant.setDescription(description);
            tenant.setCreatedAt(LocalDateTime.now());

            return tenantRepository.save(tenant);
        } catch (Exception e) {
            // В случае ошибки, можно попытаться удалить созданную БД
            try {
                jdbcTemplate.execute("DROP DATABASE IF EXISTS " + dbName);
            } catch (Exception ex) {
                System.err.println("Ошибка при удалении БД после сбоя: " + ex.getMessage());
            }
            throw new RuntimeException("Ошибка при создании тенанта: " + e.getMessage(), e);
        }
    }

    private HikariDataSource createDataSource(String dbName) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + dbName);
        ds.setUsername("postgres");
        ds.setPassword("password");
        return ds;
    }

    private void runFlywayMigration(DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(String id) {
        return tenantRepository.findById(id);
    }
}