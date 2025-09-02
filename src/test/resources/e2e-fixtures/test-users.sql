-- Test users for E2E testing
-- Sistema de Admisión MTN - Fase 0 Pre-flight
-- Password for all users: 'secret' (BCrypt hash)

-- Delete existing test data
DELETE FROM users WHERE email LIKE '%@test.com' OR email LIKE '%test.cl';

-- Insert test users with proper BCrypt hashes
-- Password: 'secret' -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- Password: 'admin123' -> $2a$10$N9qo8uLOickgx2ZMRZoMye/YQ/OZAYL/YhQwOFGE4Fy0z2vgz4pSq

-- Admin user for E2E tests
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('Admin', 'Test', 'admin@mtn.cl', '$2a$10$N9qo8uLOickgx2ZMRZoMye/YQ/OZAYL/YhQwOFGE4Fy0z2vgz4pSq', '11111111-1', '+56911111111', 'ADMIN', 'ALL_LEVELS', 'ALL_SUBJECTS', true, true, NOW());

-- Apoderado test user
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('Familia', 'Test', 'familia01@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '22222222-2', '+56922222222', 'APODERADO', NULL, NULL, true, true, NOW());

-- Teacher test user
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('María Elena', 'Test', 'maria.nueva@mtn.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '33333333-3', '+56933333333', 'TEACHER', 'PRESCHOOL', 'GENERAL', true, true, NOW());

-- Psychologist test user
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('Psicóloga', 'Test', 'psicologa@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '44444444-4', '+56944444444', 'PSYCHOLOGIST', 'ALL_LEVELS', 'ALL_SUBJECTS', true, true, NOW());

-- Cycle Director test user
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('Director', 'Test', 'director@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '55555555-5', '+56955555555', 'CYCLE_DIRECTOR', 'ALL_LEVELS', 'ALL_SUBJECTS', true, true, NOW());

-- Coordinator test user
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('Coordinadora', 'Test', 'coordinadora@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '66666666-6', '+56966666666', 'COORDINATOR', 'BASIC', 'MATHEMATICS', true, true, NOW());

-- Additional test users for comprehensive testing
INSERT INTO users (first_name, last_name, email, password, rut, phone, role, educational_level, subject, email_verified, active, created_at) VALUES
('Familia2', 'Test', 'familia02@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '77777777-7', '+56977777777', 'APODERADO', NULL, NULL, true, true, NOW()),
('Profesor', 'Inglés', 'profesor.ingles@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '88888888-8', '+56988888888', 'TEACHER', 'HIGH_SCHOOL', 'ENGLISH', true, true, NOW()),
('Usuario', 'Inactivo', 'inactivo@test.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '99999999-9', '+56999999999', 'APODERADO', NULL, NULL, false, false, NOW());

-- Verify insertions
SELECT 
    CONCAT(first_name, ' ', last_name) as nombre,
    email,
    role,
    educational_level,
    subject,
    email_verified,
    active
FROM users 
WHERE email LIKE '%@test.%' OR email LIKE '%@mtn.cl'
ORDER BY role, email;