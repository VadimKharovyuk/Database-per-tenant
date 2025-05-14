package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantAwareDataSource;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.FlightRepository;
import com.example.databasepertenant.repository.TenantRepository;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class TenantService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final TenantRepository tenantRepository;
    @Getter
    private final Map<String, FlightRepository> flightRepositories;

    public TenantService(@Qualifier("tenantDataSource") DataSource dataSource,
                         JdbcTemplate jdbcTemplate,
                         TenantRepository tenantRepository,
                         Map<String, FlightRepository> flightRepositories) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantRepository = tenantRepository;
        this.flightRepositories = flightRepositories;
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

            // Создаем EntityManagerFactory для тенанта
            EntityManagerFactory emf = createEntityManagerFactory(newDataSource, tenantId);

            // Создаем и регистрируем FlightRepository для тенанта
            FlightRepository flightRepository = new FlightRepositoryImpl(emf);
            flightRepositories.put(tenantId, flightRepository);
            System.out.println("Зарегистрирован репозиторий для тенанта: " + tenantId);

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
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(ds)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();

            Object result = flyway.migrate();
            System.out.println("Применено " + result + " миграций");
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении Flyway миграции: " + e.getMessage());
            throw e;
        }
    }

    // Метод для создания EntityManagerFactory
    private EntityManagerFactory createEntityManagerFactory(DataSource ds, String tenantId) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ds);
        em.setPackagesToScan("com.example.databasepertenant.model");

        // Устанавливаем текущего тенанта
        TenantContext.setCurrentTenant(tenantId);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        em.afterPropertiesSet();

        return em.getObject();
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(String id) {
        return tenantRepository.findById(id);
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
            throw e;
        }
    }

    /**
     * Инициализировать существующие тенанты заново
     * (выполнить миграцию для существующих БД и создать репозитории)
     */
    public void reinitializeExistingTenants() {
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            try {
                String dbName = tenant.getDbName();
                String tenantId = tenant.getId();

                System.out.println("Переинициализация тенанта: " + tenantId + " с БД: " + dbName);

                // Создаем DataSource для существующей БД
                HikariDataSource tenantDataSource = createDataSource(dbName);

                // Выполняем миграцию
                runFlywayMigration(tenantDataSource);

                // Обновляем DataSource в TenantAwareDataSource
                if (this.dataSource instanceof TenantAwareDataSource) {
                    ((TenantAwareDataSource) this.dataSource).addTenant(tenantId, tenantDataSource);
                }

                // Создаем EntityManagerFactory для тенанта
                EntityManagerFactory emf = createEntityManagerFactory(tenantDataSource, tenantId);

                // Создаем и регистрируем FlightRepository для тенанта
                FlightRepository flightRepository = new FlightRepositoryImpl(emf);
                flightRepositories.put(tenantId, flightRepository);

                System.out.println("Тенант " + tenantId + " успешно переинициализирован");
            } catch (Exception e) {
                System.err.println("Ошибка при переинициализации тенанта " + tenant.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    /**
     * Инициализировать отдельного тенанта и его репозиторий
     */
    public void initializeTenant(Tenant tenant) {
        try {
            String dbName = tenant.getDbName();
            String tenantId = tenant.getId();

            System.out.println("Инициализация тенанта: " + tenantId + " с БД: " + dbName);

            // Создаем DataSource для БД тенанта
            HikariDataSource tenantDataSource = createDataSource(dbName);

            // Обновляем DataSource в TenantAwareDataSource
            if (this.dataSource instanceof TenantAwareDataSource) {
                ((TenantAwareDataSource) this.dataSource).addTenant(tenantId, tenantDataSource);
            }

            // Создаем EntityManagerFactory для тенанта
            EntityManagerFactory emf = createEntityManagerFactory(tenantDataSource, tenantId);

            // Создаем и регистрируем FlightRepository для тенанта
            FlightRepository flightRepository = new FlightRepositoryImpl(emf);
            flightRepositories.put(tenantId, flightRepository);

            System.out.println("Тенант " + tenantId + " успешно инициализирован");
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации тенанта " + tenant.getId() + ": " + e.getMessage());
            throw e;
        }
    }

}