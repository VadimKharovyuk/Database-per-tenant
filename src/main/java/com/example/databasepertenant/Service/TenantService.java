package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantAwareDataSource;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.TenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
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

            try {
                // Выполняем миграцию Flyway для новой БД
                runFlywayMigration(newDataSource);
                System.out.println("Миграция выполнена успешно для тенанта: " + tenantId);

                // Добавляем DataSource для нового клиента
                if (dataSource instanceof TenantAwareDataSource) {
                    ((TenantAwareDataSource) dataSource).addTenant(tenantId, newDataSource);
                }

                // Сохраняем информацию о тенанте
                Tenant tenant = new Tenant();
                tenant.setId(tenantId);
                tenant.setDbName(dbName);
                tenant.setDescription(description);
                tenant.setCreatedAt(LocalDateTime.now());

                return tenantRepository.save(tenant);
            } catch (Exception e) {
                System.err.println("Ошибка при миграции или добавлении тенанта: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
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
        ds.setMaximumPoolSize(5); // Ограничиваем размер пула соединений
        return ds;
    }

    private void runFlywayMigration(DataSource ds) {
        try {
            // Настраиваем Flyway с явным указанием пути миграции
            Flyway flyway = Flyway.configure()
                    .dataSource(ds)
                    .locations("classpath:db/migration") // Правильное расположение миграций для тенантов
                    .baselineOnMigrate(true) // Важно для первой миграции
                    .load();

            // Запускаем миграцию и выводим информацию
            MigrateResult migrationsApplied = flyway.migrate();
            System.out.println("Применено " + migrationsApplied + " миграций");
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении Flyway миграции: " + e.getMessage());
            e.printStackTrace();
            throw e; // Пробрасываем исключение дальше
        }
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(String id) {
        return tenantRepository.findById(id);
    }

    /**
     * Инициализировать существующие тенанты заново
     * (выполнить миграцию для существующих БД)
     */
    public void reinitializeExistingTenants() {
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            try {
                String dbName = tenant.getDbName();
                String tenantId = tenant.getId();

                System.out.println("Переинициализация тенанта: " + tenantId + " с БД: " + dbName);

                // Создаем DataSource для существующей БД
                HikariDataSource dataSource = createDataSource(dbName);

                // Выполняем миграцию
                runFlywayMigration(dataSource);

                // Обновляем DataSource в TenantAwareDataSource
                if (this.dataSource instanceof TenantAwareDataSource) {
                    ((TenantAwareDataSource) this.dataSource).addTenant(tenantId, dataSource);
                }

                System.out.println("Тенант " + tenantId + " успешно переинициализирован");
            } catch (Exception e) {
                System.err.println("Ошибка при переинициализации тенанта " + tenant.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Инициализировать административную базу данных вручную
     */
    public void initializeAdminDatabase() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS tenants (" +
                            "    id VARCHAR(50) PRIMARY KEY," +
                            "    db_name VARCHAR(100) NOT NULL UNIQUE," +
                            "    description VARCHAR(255) NOT NULL," +
                            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");

            jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_tenants_db_name ON tenants(db_name)");

            System.out.println("Таблица tenants успешно создана/проверена");
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации базы данных администратора: " + e.getMessage());
            e.printStackTrace();
        }
    }
}