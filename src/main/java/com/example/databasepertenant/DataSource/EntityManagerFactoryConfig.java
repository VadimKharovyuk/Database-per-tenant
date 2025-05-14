package com.example.databasepertenant.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EntityManagerFactoryConfig {

    @Bean(name = "company1EntityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean company1EntityManagerFactoryBean(
            @Qualifier("tenantDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.databasepertenant.model");

        // Устанавливаем текущего тенанта перед созданием EntityManagerFactory
        TenantContext.setCurrentTenant("company1");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "company1EntityManagerFactory")
    public EntityManagerFactory company1EntityManagerFactory(
            @Qualifier("company1EntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean factoryBean) {
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Bean(name = "company2EntityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean company2EntityManagerFactoryBean(
            @Qualifier("tenantDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.databasepertenant.model");

        // Устанавливаем текущего тенанта перед созданием EntityManagerFactory
        TenantContext.setCurrentTenant("company2");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "company2EntityManagerFactory")
    public EntityManagerFactory company2EntityManagerFactory(
            @Qualifier("company2EntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean factoryBean) {
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}