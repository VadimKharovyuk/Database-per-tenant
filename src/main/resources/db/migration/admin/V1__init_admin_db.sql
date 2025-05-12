-- db/migration/admin/V1__init_admin_db.sql

-- Таблица тенантов (компаний)
CREATE TABLE IF NOT EXISTS tenants (
                                       id VARCHAR(50) PRIMARY KEY,
                                       db_name VARCHAR(100) NOT NULL UNIQUE,
                                       description VARCHAR(255) NOT NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска тенантов по имени базы данных
CREATE INDEX IF NOT EXISTS idx_tenants_db_name ON tenants(db_name);