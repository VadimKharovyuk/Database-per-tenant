package com.example.databasepertenant.Controller;
import com.example.databasepertenant.DataSource.TenantContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class DebugController {


    private final DataSource dataSource;

    public DebugController(@Qualifier("tenantDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }



    @GetMapping("/test-db/{tenantId}")
    public ResponseEntity<String> testDatabase(@PathVariable String tenantId) {
        try {
            TenantContext.setTenantId(tenantId);
            System.out.println("Тестирование подключения к БД для тенанта: " + tenantId);

            // Используем внедрённый DataSource, который должен быть мультитенантным
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet tables = metaData.getTables(null, "public", "%", new String[]{"TABLE"});

                List<String> tableNames = new ArrayList<>();
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    tableNames.add(tableName);
                }

                return ResponseEntity.ok("Таблицы в базе данных: " + String.join(", ", tableNames));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка при подключении к БД: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }


    @GetMapping("/headers")
    public ResponseEntity<String> checkHeaders(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        StringBuilder response = new StringBuilder();
        response.append("Получен заголовок X-Tenant-ID: ").append(tenantId != null ? tenantId : "NULL").append("\n");
        response.append("Текущий тенант в контексте: ").append(TenantContext.getTenantId());

        return ResponseEntity.ok(response.toString());
    }
}