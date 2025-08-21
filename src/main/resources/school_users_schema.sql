-- Script SQL para crear las tablas del sistema de usuarios del colegio

-- Agregar nuevas columnas a la tabla usuarios existente
ALTER TABLE usuarios 
ADD COLUMN first_name VARCHAR(255),
ADD COLUMN last_name VARCHAR(255),
ADD COLUMN phone VARCHAR(20),
ADD COLUMN profile_image VARCHAR(500),
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN updated_at TIMESTAMP;

-- Actualizar los roles existentes si es necesario
-- Los nuevos roles se manejan automáticamente por el enum RolUsuario

-- Tabla para profesores (solo Matemática, Lenguaje, Inglés)
CREATE TABLE professors (
    id BIGINT PRIMARY KEY,
    department VARCHAR(255),
    years_of_experience INTEGER,
    is_admin BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla para materias de profesores
CREATE TABLE professor_subjects (
    professor_id BIGINT NOT NULL,
    subject VARCHAR(50) NOT NULL,
    PRIMARY KEY (professor_id, subject),
    FOREIGN KEY (professor_id) REFERENCES professors(id) ON DELETE CASCADE
);

-- Tabla para niveles asignados a profesores
CREATE TABLE professor_grades (
    professor_id BIGINT NOT NULL,
    grade VARCHAR(20) NOT NULL,
    PRIMARY KEY (professor_id, grade),
    FOREIGN KEY (professor_id) REFERENCES professors(id) ON DELETE CASCADE
);

-- Tabla para calificaciones de profesores
CREATE TABLE professor_qualifications (
    professor_id BIGINT NOT NULL,
    qualification VARCHAR(255) NOT NULL,
    PRIMARY KEY (professor_id, qualification),
    FOREIGN KEY (professor_id) REFERENCES professors(id) ON DELETE CASCADE
);

-- Tabla para personal de kinder
CREATE TABLE kinder_teachers (
    id BIGINT PRIMARY KEY,
    assigned_level VARCHAR(20) NOT NULL, -- 'PREKINDER' o 'KINDER'
    years_of_experience INTEGER,
    FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla para especializaciones del personal de kinder
CREATE TABLE kinder_teacher_specializations (
    teacher_id BIGINT NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, specialization),
    FOREIGN KEY (teacher_id) REFERENCES kinder_teachers(id) ON DELETE CASCADE
);

-- Tabla para calificaciones del personal de kinder
CREATE TABLE kinder_teacher_qualifications (
    teacher_id BIGINT NOT NULL,
    qualification VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, qualification),
    FOREIGN KEY (teacher_id) REFERENCES kinder_teachers(id) ON DELETE CASCADE
);

-- Tabla para psicólogos
CREATE TABLE psychologists (
    id BIGINT PRIMARY KEY,
    specialty VARCHAR(100) NOT NULL, -- PsychologySpecialty enum
    license_number VARCHAR(100),
    can_conduct_interviews BOOLEAN NOT NULL DEFAULT false,
    can_perform_psychological_evaluations BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla para niveles asignados a psicólogos
CREATE TABLE psychologist_grades (
    psychologist_id BIGINT NOT NULL,
    grade VARCHAR(20) NOT NULL,
    PRIMARY KEY (psychologist_id, grade),
    FOREIGN KEY (psychologist_id) REFERENCES psychologists(id) ON DELETE CASCADE
);

-- Tabla para áreas especializadas de psicólogos
CREATE TABLE psychologist_specialized_areas (
    psychologist_id BIGINT NOT NULL,
    area VARCHAR(255) NOT NULL,
    PRIMARY KEY (psychologist_id, area),
    FOREIGN KEY (psychologist_id) REFERENCES psychologists(id) ON DELETE CASCADE
);

-- Tabla para personal de apoyo
CREATE TABLE support_staff (
    id BIGINT PRIMARY KEY,
    staff_type VARCHAR(100) NOT NULL, -- SupportStaffType enum
    department VARCHAR(255),
    can_access_reports BOOLEAN NOT NULL DEFAULT false,
    can_manage_schedules BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla para responsabilidades del personal de apoyo
CREATE TABLE support_staff_responsibilities (
    staff_id BIGINT NOT NULL,
    responsibility VARCHAR(255) NOT NULL,
    PRIMARY KEY (staff_id, responsibility),
    FOREIGN KEY (staff_id) REFERENCES support_staff(id) ON DELETE CASCADE
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_active ON usuarios(is_active);
CREATE INDEX idx_usuarios_role_active ON usuarios(rol, is_active);
CREATE INDEX idx_professor_subjects ON professor_subjects(subject);
CREATE INDEX idx_psychologist_specialty ON psychologists(specialty);
CREATE INDEX idx_support_staff_type ON support_staff(staff_type);

-- Comentarios para documentación
COMMENT ON TABLE professors IS 'Profesores de Matemática, Lenguaje e Inglés';
COMMENT ON TABLE kinder_teachers IS 'Personal de prekinder y kinder';
COMMENT ON TABLE psychologists IS 'Psicólogos del colegio';
COMMENT ON TABLE support_staff IS 'Personal de apoyo administrativo y técnico';

COMMENT ON COLUMN usuarios.first_name IS 'Nombre del usuario';
COMMENT ON COLUMN usuarios.last_name IS 'Apellido del usuario';
COMMENT ON COLUMN usuarios.is_active IS 'Indica si el usuario está activo en el sistema';
COMMENT ON COLUMN professors.is_admin IS 'Indica si el profesor tiene permisos administrativos';
COMMENT ON COLUMN kinder_teachers.assigned_level IS 'Nivel asignado: PREKINDER o KINDER';
COMMENT ON COLUMN psychologists.can_conduct_interviews IS 'Puede realizar entrevistas';
COMMENT ON COLUMN psychologists.can_perform_psychological_evaluations IS 'Puede realizar evaluaciones psicológicas';
COMMENT ON COLUMN support_staff.can_access_reports IS 'Puede acceder a reportes';
COMMENT ON COLUMN support_staff.can_manage_schedules IS 'Puede gestionar horarios';