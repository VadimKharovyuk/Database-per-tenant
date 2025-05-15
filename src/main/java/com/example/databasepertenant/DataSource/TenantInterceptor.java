package com.example.databasepertenant.DataSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
//            System.out.println("Set tenant ID: " + tenantId);
        } else {
//            System.out.println("No X-Tenant-ID header found in request to: " + request.getRequestURI());
//            // Устанавливаем default для запросов без заголовка
            TenantContext.setTenantId("default");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}