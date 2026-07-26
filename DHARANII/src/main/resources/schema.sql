-- Database Schema Script for DTC (MySQL and H2 compatible)


-- 1. Depots Table
CREATE TABLE IF NOT EXISTS depots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 3. Users Table (Admin, Depot Manager, Scheduler, Driver, Conductor)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- 4. Buses Table
CREATE TABLE IF NOT EXISTS buses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bus_number VARCHAR(20) NOT NULL UNIQUE, -- e.g., DL-1PC-1234
    model VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    fuel_type VARCHAR(20) NOT NULL, -- CNG, ELECTRIC, DIESEL
    status VARCHAR(30) DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE, OUT_OF_SERVICE
    depot_id BIGINT,
    mileage_km DOUBLE DEFAULT 0.0,
    last_service_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (depot_id) REFERENCES depots(id) ON DELETE SET NULL
);

-- 5. Drivers Table
CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    license_expiry_date DATE NOT NULL,
    experience_years INT NOT NULL,
    status VARCHAR(30) DEFAULT 'AVAILABLE', -- AVAILABLE, ON_TRIP, LEAVE, SUSPENDED
    depot_id BIGINT,
    assigned_bus_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (depot_id) REFERENCES depots(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_bus_id) REFERENCES buses(id) ON DELETE SET NULL
);

-- 6. Conductors Table
CREATE TABLE IF NOT EXISTS conductors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) DEFAULT 'AVAILABLE', -- AVAILABLE, ON_TRIP, LEAVE
    depot_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (depot_id) REFERENCES depots(id) ON DELETE SET NULL
);

-- 7. Routes Table
CREATE TABLE IF NOT EXISTS routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_number VARCHAR(20) NOT NULL UNIQUE, -- e.g., 502, 419, 727
    start_location VARCHAR(100) NOT NULL,
    end_location VARCHAR(100) NOT NULL,
    distance_km DOUBLE NOT NULL,
    estimated_duration_mins INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, TEMPORARILY_CLOSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Stops Table (Route details)
CREATE TABLE IF NOT EXISTS stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    stop_name VARCHAR(100) NOT NULL,
    sequence_number INT NOT NULL,
    distance_from_start DOUBLE NOT NULL,
    estimated_time_from_start INT NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    UNIQUE KEY uq_route_sequence (route_id, sequence_number)
);

-- 9. Schedules Table
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    conductor_id BIGINT NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    shift_type VARCHAR(20) NOT NULL, -- MORNING, EVENING, NIGHT
    is_peak_hour BOOLEAN DEFAULT FALSE,
    schedule_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE RESTRICT,
    FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE RESTRICT,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE RESTRICT,
    FOREIGN KEY (conductor_id) REFERENCES conductors(id) ON DELETE RESTRICT
);

-- 10. Trips Table (Real-time operation instances)
CREATE TABLE IF NOT EXISTS trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    trip_date DATE NOT NULL,
    actual_departure_time DATETIME,
    actual_arrival_time DATETIME,
    delay_minutes INT DEFAULT 0,
    status VARCHAR(30) DEFAULT 'SCHEDULED', -- SCHEDULED, EN_ROUTE, COMPLETED, DELAYED, CANCELLED
    current_stop VARCHAR(100),
    notes TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE RESTRICT
);

-- 11. Maintenance Records Table
CREATE TABLE IF NOT EXISTS maintenance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bus_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    service_type VARCHAR(50) NOT NULL, -- ROUTINE, REPAIR, INSPECTION
    description TEXT,
    cost DOUBLE DEFAULT 0.0,
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED
    technician_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE
);

-- 12. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL, -- MAINTENANCE, LICENSE_EXPIRY, ROUTE_CHANGE, DELAY
    read_status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 13. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optimization Indexes
CREATE INDEX idx_bus_status ON buses(status);
CREATE INDEX idx_driver_status ON drivers(status);
CREATE INDEX idx_conductor_status ON conductors(status);
CREATE INDEX idx_schedule_date ON schedules(schedule_date);
CREATE INDEX idx_trip_status ON trips(status);
CREATE INDEX idx_trip_date ON trips(trip_date);
CREATE INDEX idx_stop_route ON stops(route_id);
