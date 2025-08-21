-- Crear un admin de prueba con contraseña conocida
-- Contraseña: admin123 (encriptada con BCrypt)

INSERT INTO users (first_name, last_name, email, password, role, active, email_verified, created_at, rut)
VALUES (
  'Admin', 
  'Test', 
  'admin@mtn.cl', 
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
  'ADMIN', 
  true, 
  true, 
  NOW(),
  '12345678-9'
) ON CONFLICT (email) DO UPDATE SET 
  password = EXCLUDED.password,
  active = true,
  email_verified = true;
