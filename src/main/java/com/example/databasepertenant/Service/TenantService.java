package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantAwareDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class TenantService {

    @Autowired
    @Qualifier("tenantDataSource")
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createTenant(String tenantId, String dbName) {
        // Создаем новую БД - используем jdbcTemplate, который подключен к adminDataSource
        jdbcTemplate.execute("CREATE DATABASE " + dbName);
        System.out.println("Создание тенанта с ID: [" + tenantId + "] и базой данных: [" + dbName + "]");

        // Создаем DataSource для новой БД
        HikariDataSource newDataSource = new HikariDataSource();
        newDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/" + dbName);
        newDataSource.setUsername("postgres");
        newDataSource.setPassword("password");

        // Добавляем DataSource для нового клиента
        if (dataSource instanceof TenantAwareDataSource) {
            ((TenantAwareDataSource) dataSource).addTenant(tenantId, newDataSource);
        }

        // Выполняем миграцию Flyway
        Flyway flyway = Flyway.configure()
                .dataSource(newDataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }
}