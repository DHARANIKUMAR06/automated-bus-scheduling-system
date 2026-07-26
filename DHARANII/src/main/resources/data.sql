-- Automated Bus Scheduling and Route Management System for DTC
-- Seed Data Script for MySQL 8.0+

-- DB Selection and Truncation commented out for H2 compatibility


-- 1. Seed Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_DEPOT_MANAGER'),
(3, 'ROLE_SCHEDULER'),
(4, 'ROLE_DRIVER'),
(5, 'ROLE_CONDUCTOR');

-- 2. Seed Depots
INSERT INTO depots (id, name, location, capacity) VALUES
(1, 'Rajghat Depot-I', 'Jawaharlal Nehru Marg, Near Raj Ghat, New Delhi', 120),
(2, 'Indraprastha Depot', 'Near Vikas Minar, IP Estate, New Delhi', 150),
(3, 'Sukhdev Vihar Depot', 'Okhla Road, Near Jamia Millia, New Delhi', 100);

-- 3. Seed Users
-- Password is 'admin123' BCrypt hash: $2a$10$dXJ3ADWyyTXHQ3JJhecl3u96p4e.g9j3tUqG54d9u0U6G6cQ6H1e2
INSERT INTO users (id, username, email, password, role_id, full_name, phone, status) VALUES
(1, 'admin', 'admin@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 'DTC System Administrator', '9876543210', 'ACTIVE'),
(2, 'manager', 'manager@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2, 'Rajesh Kumar (Depot Manager)', '9876543211', 'ACTIVE'),
(3, 'scheduler', 'scheduler@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 3, 'Anil Sharma (Chief Scheduler)', '9876543212', 'ACTIVE'),
(4, 'driver1', 'driver1@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 4, 'Sukhvinder Singh (Driver)', '9876543213', 'ACTIVE'),
(5, 'driver2', 'driver2@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 4, 'Mohammad Riyaz (Driver)', '9876543214', 'ACTIVE'),
(6, 'conductor1', 'conductor1@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 5, 'Ramesh Chand (Conductor)', '9876543215', 'ACTIVE'),
(7, 'conductor2', 'conductor2@dtc.delhi.gov.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 5, 'Suresh Lal (Conductor)', '9876543216', 'ACTIVE');

-- 4. Seed Buses
INSERT INTO buses (id, bus_number, model, capacity, fuel_type, status, depot_id, mileage_km, last_service_date) VALUES
(1, 'DL-1PD-2034', 'Tata Starbus Hybrid', 45, 'CNG', 'ACTIVE', 1, 12450.5, '2026-06-10'),
(2, 'DL-1PD-4412', 'JBM Ecolife Electric', 35, 'ELECTRIC', 'ACTIVE', 1, 8500.2, '2026-06-15'),
(3, 'DL-1PD-8876', 'Tata LPO 1613', 50, 'CNG', 'ACTIVE', 2, 45200.0, '2026-05-20'),
(4, 'DL-1PD-9901', 'Ashok Leyland JanBus', 40, 'DIESEL', 'MAINTENANCE', 2, 62100.8, '2026-07-10'),
(5, 'DL-1PD-5561', 'JBM Ecolife Electric', 35, 'ELECTRIC', 'ACTIVE', 3, 3100.0, '2026-07-02');

-- 5. Seed Drivers
INSERT INTO drivers (id, user_id, license_number, license_expiry_date, experience_years, status, depot_id, assigned_bus_id) VALUES
(1, 4, 'DL-1420100084321', '2028-12-15', 12, 'AVAILABLE', 1, 1),
(2, 5, 'DL-1420120095654', '2026-08-10', 8, 'AVAILABLE', 1, 2); -- Expiring soon for notification triggers!

-- 6. Seed Conductors
INSERT INTO conductors (id, user_id, employee_id, status, depot_id) VALUES
(1, 6, 'EMP-CON-8841', 'AVAILABLE', 1),
(2, 7, 'EMP-CON-9023', 'AVAILABLE', 1);

-- 7. Seed Routes
INSERT INTO routes (id, route_number, start_location, end_location, distance_km, estimated_duration_mins, status) VALUES
(1, '419', 'Ambedkar Nagar Terminal', 'Old Delhi Railway Station', 24.5, 75, 'ACTIVE'),
(2, '502', 'Mehrauli Terminal', 'Mori Gate Terminal', 28.0, 90, 'ACTIVE'),
(3, '727', 'Jawaharlal Nehru Stadium', 'Dwarka Sector 21 Metro Station', 32.2, 105, 'ACTIVE');

-- 8. Seed Stops
-- Stops for Route 419
INSERT INTO stops (id, route_id, stop_name, sequence_number, distance_from_start, estimated_time_from_start) VALUES
(1, 1, 'Ambedkar Nagar Terminal', 1, 0.0, 0),
(2, 1, 'Pushpa Vihar', 2, 2.5, 8),
(3, 1, 'Saket Crossing', 3, 4.8, 15),
(4, 1, 'Chirag Delhi', 4, 7.2, 22),
(5, 1, 'Moolchand Hospital', 5, 11.0, 35),
(6, 1, 'Delhi Gate', 6, 18.5, 55),
(7, 1, 'Old Delhi Railway Station', 7, 24.5, 75);

-- Stops for Route 502
INSERT INTO stops (id, route_id, stop_name, sequence_number, distance_from_start, estimated_time_from_start) VALUES
(8, 2, 'Mehrauli Terminal', 1, 0.0, 0),
(9, 2, 'Qutub Minar Metro Station', 2, 1.8, 5),
(10, 2, 'Adchini', 3, 4.5, 12),
(11, 2, 'AIIMS', 4, 9.2, 25),
(12, 2, 'Connaught Place (Super Bazar)', 5, 17.8, 50),
(13, 2, 'ISBT Kashmere Gate', 6, 25.5, 78),
(14, 2, 'Mori Gate Terminal', 7, 28.0, 90);

-- 9. Seed Schedules (For current date)
-- We will pre-populate schedules for a typical operational day
INSERT INTO schedules (id, route_id, bus_id, driver_id, conductor_id, departure_time, arrival_time, shift_type, is_peak_hour, schedule_date, status) VALUES
(1, 1, 1, 1, 1, '08:00:00', '09:15:00', 'MORNING', TRUE, '2026-07-14', 'ACTIVE'),
(2, 1, 2, 2, 2, '08:30:00', '09:45:00', 'MORNING', TRUE, '2026-07-14', 'ACTIVE'),
(3, 2, 1, 1, 1, '11:00:00', '12:30:00', 'MORNING', FALSE, '2026-07-14', 'ACTIVE');

-- 10. Seed Trips (For today's operations)
INSERT INTO trips (id, schedule_id, trip_date, actual_departure_time, actual_arrival_time, delay_minutes, status, current_stop, notes) VALUES
(1, 1, '2026-07-14', TIMESTAMP '2026-07-14 08:05:00', TIMESTAMP '2026-07-14 09:22:00', 7, 'COMPLETED', 'Old Delhi Railway Station', 'Slight traffic at Chirag Delhi'),
(2, 2, '2026-07-14', TIMESTAMP '2026-07-14 08:32:00', NULL, 15, 'EN_ROUTE', 'Chirag Delhi', 'Delayed due to congestion on Ring Road'),
(3, 3, '2026-07-14', NULL, NULL, 0, 'SCHEDULED', 'Mehrauli Terminal', 'Yet to depart');

-- 11. Seed Maintenance Records
INSERT INTO maintenance_records (id, bus_id, service_date, service_type, description, cost, status, technician_notes) VALUES
(1, 1, '2026-06-10', 'ROUTINE', 'Engine oil change, brake fluid top up and air filter cleaning.', 4500.0, 'COMPLETED', 'Next service recommended in 5000 km.'),
(2, 4, '2026-07-10', 'REPAIR', 'AC compressor replacement and coolant leak repair.', 18200.0, 'IN_PROGRESS', 'Waiting for JBM spare parts delivery.');

-- 12. Seed Notifications
INSERT INTO notifications (id, user_id, message, type, read_status) VALUES
(1, 1, 'Bus DL-1PD-9901 is currently under maintenance. Estimated completion: 2026-07-15.', 'MAINTENANCE', FALSE),
(2, 1, 'Driver Mohammad Riyaz: License expiring on 2026-08-10 (under 30 days). Please arrange for renewal.', 'LICENSE_EXPIRY', FALSE),
(3, 3, 'Trip #2 (Route 419) is running late by 15 minutes due to heavy traffic on Chirag Delhi stretch.', 'DELAY', FALSE);

-- 13. Seed Audit Logs
INSERT INTO audit_logs (id, username, action, details, ip_address, timestamp) VALUES
(1, 'admin', 'USER_LOGIN', 'Administrator logged in successfully', '127.0.0.1', '2026-07-14 08:00:00'),
(2, 'scheduler', 'AUTO_SCHEDULE_GENERATE', 'Generated 14 schedule slots for Route 419', '127.0.0.1', '2026-07-14 09:00:00');
