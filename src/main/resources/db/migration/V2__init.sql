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
-- Create flights table for tenant databases
CREATE TABLE IF NOT EXISTS flights (
                                       id SERIAL PRIMARY KEY,
                                       flight_number VARCHAR(20) NOT NULL,
                                       origin VARCHAR(100) NOT NULL,
                                       destination VARCHAR(100) NOT NULL,
                                       departure_time TIMESTAMP NOT NULL,
                                       arrival_time TIMESTAMP NOT NULL,
                                       price DECIMAL(10, 2) NOT NULL,
                                       available_seats INTEGER NOT NULL,
                                       aircraft VARCHAR(50) NOT NULL
);

-- Add index for common search queries
CREATE INDEX IF NOT EXISTS idx_flights_origin_destination ON flights(origin, destination);
CREATE INDEX IF NOT EXISTS idx_flights_departure_time ON flights(departure_time);
CREATE INDEX IF NOT EXISTS idx_flights_flight_number ON flights(flight_number);

-- Optional: Add some sample data for testing
INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, price, available_seats, aircraft)
VALUES ('FL001', 'Moscow', 'London', '2025-06-01 08:00:00', '2025-06-01 10:30:00', 250.00, 120, 'Boeing 737');

INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, price, available_seats, aircraft)
VALUES ('FL002', 'London', 'New York', '2025-06-01 12:00:00', '2025-06-01 20:00:00', 450.00, 200, 'Boeing 777');

INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, price, available_seats, aircraft)
VALUES ('FL003', 'Moscow', 'Paris', '2025-06-02 09:00:00', '2025-06-02 11:30:00', 180.00, 150, 'Airbus A320');

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