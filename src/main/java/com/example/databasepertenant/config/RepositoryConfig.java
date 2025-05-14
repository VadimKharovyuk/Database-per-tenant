package com.example.databasepertenant.config;

import com.example.databasepertenant.Service.FlightRepositoryImpl;
import com.example.databasepertenant.repository.FlightRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class RepositoryConfig {

    @Bean
    @DependsOn({"company1Repository", "company2Repository"})
    public Map<String, FlightRepository> flightRepositories(
            @Qualifier("company1Repository") FlightRepository company1Repository,
            @Qualifier("company2Repository") FlightRepository company2Repository) {

        Map<String, FlightRepository> repositories = new HashMap<>();
        repositories.put("company1", company1Repository);
        repositories.put("company2", company2Repository);
        return repositories;
    }

    @Bean
    @Qualifier("company1Repository")
    public FlightRepository company1Repository(
            @Qualifier("company1EntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean emf) {
        return new FlightRepositoryImpl(emf.getObject());
    }

    @Bean
    @Qualifier("company2Repository")
    public FlightRepository company2Repository(
            @Qualifier("company2EntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean emf) {
        return new FlightRepositoryImpl(emf.getObject());
    }
}