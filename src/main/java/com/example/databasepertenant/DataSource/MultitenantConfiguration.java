package com.example.databasepertenant.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultitenantConfiguration {

    @Bean(name = "tenantDataSource")
    public DataSource tenantDataSource() {
        TenantAwareDataSource dataSource = new TenantAwareDataSource();

        // Создаём дефолтный DataSource
        HikariDataSource defaultDataSource = new HikariDataSource();
        defaultDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/master_db");
        defaultDataSource.setUsername("postgres");
        defaultDataSource.setPassword("password");

        // Настраиваем defaultTargetDataSource
        dataSource.setDefaultTargetDataSource(defaultDataSource);

        // Создаём и настраиваем targetDataSources
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("default", defaultDataSource);
        dataSource.setTargetDataSources(targetDataSources);

        // Инициализируем
        dataSource.afterPropertiesSet();

        return dataSource;
    }

    @Bean(name = "adminDataSource")
    public DataSource adminDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/master_db");
        dataSource.setUsername("postgres");
        dataSource.setPassword("password");
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("tenantDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.databasepertenant.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("adminDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}