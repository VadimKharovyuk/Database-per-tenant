package com.example.databasepertenant.DataSource;

import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.TenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    public TenantInitializer(TenantRepository tenantRepository,
                             @Qualifier("tenantDataSource") DataSource dataSource ,
                             TenantService tenantService ,JdbcTemplate jdbcTemplate) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
        this.tenantService =tenantService ;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Создаем таблицы в основной базе данных
        createMasterDatabaseTables();

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
            System.err.println("Ошибка при инициализации тенантов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createMasterDatabaseTables() {
        // Создание таблицы ролей
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS roles (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(50) NOT NULL UNIQUE,
                description VARCHAR(255)
            )
        """);

        // Добавление базовых ролей, если их еще нет
        jdbcTemplate.update("""
            INSERT INTO roles (name, description) 
            VALUES 
            ('ROLE_USER', 'Стандартный пользователь'),
            ('ROLE_ADMIN', 'Администратор системы'),
            ('ROLE_PASSENGER', 'Пассажир')
            ON CONFLICT (name) DO NOTHING
        """);

        // Создание таблицы пользователей
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Создание таблицы связи пользователей и ролей
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_roles (
                user_id BIGINT REFERENCES users(id),
                role_id BIGINT REFERENCES roles(id),
                PRIMARY KEY (user_id, role_id)
            )
        """);
    }

}