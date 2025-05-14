package com.example.databasepertenant.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class TenantAwareDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> tenantDataSources = new HashMap<>();

    public TenantAwareDataSource(DataSource defaultDataSource) {
        // Настраиваем defaultTargetDataSource
        super.setDefaultTargetDataSource(defaultDataSource);

        // Создаём и настраиваем начальные targetDataSources
        tenantDataSources.put("default", defaultDataSource);
        super.setTargetDataSources(new HashMap<>(tenantDataSources));

        // Инициализируем
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "default";
    }

    public void addTenant(String tenantId, DataSource dataSource) {
        tenantDataSources.put(tenantId, dataSource);
        setTargetDataSources(new HashMap<>(tenantDataSources));
        afterPropertiesSet(); // Важно вызвать это для обновления решающего DataSource
    }

    public void removeTenant(String tenantId) {
        // Удаляем из нашей карты
        tenantDataSources.remove(tenantId);

        // Обновляем карту источников данных в родительском классе
        super.setTargetDataSources(new HashMap<>(tenantDataSources));

        // Переинициализируем
        super.afterPropertiesSet();
    }
}