package com.example.databasepertenant.DataSource;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Получить идентификатор текущего тенанта
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Альтернативный метод для получения текущего тенанта
     * (для использования в TenantAwareDataSource)
     */
    public static String getTenantId() {
        return getCurrentTenant();
    }

    /**
     * Установить идентификатор текущего тенанта
     */
    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Альтернативный метод для установки текущего тенанта
     * (для использования в TenantInterceptor)
     */
    public static void setTenantId(String tenantId) {
        setCurrentTenant(tenantId);
    }

    /**
     * Очистить контекст текущего тенанта
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}