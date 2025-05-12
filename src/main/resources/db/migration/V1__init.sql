-- Создание основных таблиц для каждого тенанта

-- Таблица пользователей
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица ролей
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT
);

-- Таблица для связи пользователей и ролей (many-to-many)
CREATE TABLE user_roles (
                            user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                            role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Таблица услуг компании
CREATE TABLE services (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          price DECIMAL(10, 2) NOT NULL,
                          active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Добавление нескольких услуг по умолчанию
INSERT INTO services (name, description, price) VALUES
                                                    ('Базовая подписка', 'Основной пакет услуг с ограниченным функционалом', 9.99),
                                                    ('Стандартная подписка', 'Расширенный пакет услуг для малого бизнеса', 19.99),
                                                    ('Премиум подписка', 'Полный доступ ко всем функциям системы', 49.99);

-- Создание индексов для быстрого поиска
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Добавление базовых ролей
INSERT INTO roles (name, description) VALUES
                                          ('ADMIN', 'Administrator with full access'),
                                          ('USER', 'Regular user with limited access'),
                                          ('MANAGER', 'Manager with department level access');

-- Добавление администратора по умолчанию (пароль нужно заменить на хешированный в реальном приложении)
INSERT INTO users (username, email, password_hash, first_name, last_name)
VALUES ('admin', 'admin@company.com', 'change_this_to_hashed_password', 'Admin', 'User');

-- Назначение роли администратора
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1); -- admin получает роль ADMIN