package com.example.databasepertenant.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class TenantAwareDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenantId();
    }

    public void addTenant(String tenantId, DataSource dataSource) {
        Map<Object, Object> dataSources = new HashMap<>(getResolvedDataSources());
        dataSources.put(tenantId, dataSource);
        setTargetDataSources(dataSources);
        afterPropertiesSet();
    }
}
