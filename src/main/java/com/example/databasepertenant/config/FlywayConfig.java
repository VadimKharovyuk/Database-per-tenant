package com.example.databasepertenant.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean(name = "adminFlyway")
    public Flyway adminFlyway(@Qualifier("defaultDataSource") DataSource dataSource) {
        try {
            System.out.println("Начинаем миграцию admin...");

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/admin")
                    .baselineOnMigrate(true)
                    .load();

            MigrateResult applied = flyway.migrate();
            System.out.println("Применено " + applied + " миграций для admin");

            return flyway;
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении миграции admin: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}