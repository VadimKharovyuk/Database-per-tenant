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
-- Таблица рейсов
CREATE TABLE flights (
                         id SERIAL PRIMARY KEY,
                         flight_number VARCHAR(20) NOT NULL,
                         departure_airport VARCHAR(100) NOT NULL,
                         arrival_airport VARCHAR(100) NOT NULL,
                         departure_time TIMESTAMP NOT NULL,
                         arrival_time TIMESTAMP NOT NULL,
                         available_seats INTEGER NOT NULL,
                         base_price DECIMAL(10, 2) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица бронирований
CREATE TABLE bookings (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                          flight_id INTEGER REFERENCES flights(id) ON DELETE CASCADE,
                          passenger_name VARCHAR(100) NOT NULL,
                          passenger_email VARCHAR(100) NOT NULL,
                          seat_number VARCHAR(10) NOT NULL,
                          paid_amount DECIMAL(10, 2) NOT NULL,
                          booking_time TIMESTAMP NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для быстрого поиска
CREATE INDEX idx_flights_departure ON flights(departure_time);
CREATE INDEX idx_flights_airports ON flights(departure_airport, arrival_airport);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_flight ON bookings(flight_id);

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