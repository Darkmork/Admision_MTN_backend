-- Script para crear la tabla de entrevistas
-- Ejecutar con: PGPASSWORD=admin123 /opt/homebrew/Cellar/postgresql@15/15.13/bin/psql -h localhost -U admin -d "Admisión_MTN_DB" -f "create_interviews_table.sql"

CREATE TABLE IF NOT EXISTS interviews (
    id SERIAL PRIMARY KEY,
    
    -- Relaciones
    application_id BIGINT NOT NULL,
    interviewer_user_id BIGINT NOT NULL,
    
    -- Estado y tipo de entrevista
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'RESCHEDULED')),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INDIVIDUAL', 'FAMILY', 'PSYCHOLOGICAL', 'ACADEMIC', 'BEHAVIORAL')),
    mode VARCHAR(20) NOT NULL CHECK (mode IN ('IN_PERSON', 'VIRTUAL', 'HYBRID')),
    
    -- Programación
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    duration INTEGER NOT NULL CHECK (duration >= 15 AND duration <= 480), -- en minutos
    
    -- Ubicación y acceso
    location VARCHAR(500),
    virtual_meeting_link VARCHAR(1000),
    
    -- Notas y preparación
    notes TEXT,
    preparation TEXT,
    
    -- Resultados de la entrevista
    result VARCHAR(20) CHECK (result IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE', 'PENDING_REVIEW', 'REQUIRES_FOLLOW_UP')),
    score DECIMAL(3,1) CHECK (score >= 1.0 AND score <= 10.0), -- 1.0 a 10.0
    recommendations TEXT,
    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    
    -- Constraints
    CONSTRAINT fk_interview_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_interviewer FOREIGN KEY (interviewer_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    
    -- Índices únicos para evitar duplicados
    CONSTRAINT unique_interviewer_datetime UNIQUE (interviewer_user_id, scheduled_date, scheduled_time)
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_interviews_application_id ON interviews(application_id);
CREATE INDEX IF NOT EXISTS idx_interviews_interviewer_id ON interviews(interviewer_user_id);
CREATE INDEX IF NOT EXISTS idx_interviews_status ON interviews(status);
CREATE INDEX IF NOT EXISTS idx_interviews_type ON interviews(type);
CREATE INDEX IF NOT EXISTS idx_interviews_mode ON interviews(mode);
CREATE INDEX IF NOT EXISTS idx_interviews_scheduled_date ON interviews(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_interviews_result ON interviews(result);
CREATE INDEX IF NOT EXISTS idx_interviews_follow_up ON interviews(follow_up_required) WHERE follow_up_required = TRUE;
CREATE INDEX IF NOT EXISTS idx_interviews_completed_at ON interviews(completed_at);

-- Índice compuesto para búsquedas comunes
CREATE INDEX IF NOT EXISTS idx_interviews_date_status ON interviews(scheduled_date, status);
CREATE INDEX IF NOT EXISTS idx_interviews_interviewer_date ON interviews(interviewer_user_id, scheduled_date);

-- Comentarios para documentación
COMMENT ON TABLE interviews IS 'Tabla para gestionar las entrevistas de admisión';
COMMENT ON COLUMN interviews.status IS 'Estado actual de la entrevista: SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW, RESCHEDULED';
COMMENT ON COLUMN interviews.type IS 'Tipo de entrevista: INDIVIDUAL, FAMILY, PSYCHOLOGICAL, ACADEMIC, BEHAVIORAL';
COMMENT ON COLUMN interviews.mode IS 'Modalidad: IN_PERSON, VIRTUAL, HYBRID';
COMMENT ON COLUMN interviews.duration IS 'Duración en minutos (15-480)';
COMMENT ON COLUMN interviews.score IS 'Puntuación de 1.0 a 10.0';
COMMENT ON COLUMN interviews.follow_up_required IS 'Indica si la entrevista requiere seguimiento';

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_interviews_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para actualizar updated_at
DROP TRIGGER IF EXISTS trigger_update_interviews_updated_at ON interviews;
CREATE TRIGGER trigger_update_interviews_updated_at
    BEFORE UPDATE ON interviews
    FOR EACH ROW
    EXECUTE FUNCTION update_interviews_updated_at();

-- Función para validar que las entrevistas completadas tengan resultado
CREATE OR REPLACE FUNCTION validate_completed_interview()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND NEW.result IS NULL THEN
        RAISE EXCEPTION 'Las entrevistas completadas deben tener un resultado';
    END IF;
    
    IF NEW.status = 'COMPLETED' AND NEW.completed_at IS NULL THEN
        NEW.completed_at = CURRENT_TIMESTAMP;
    END IF;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para validar entrevistas completadas
DROP TRIGGER IF EXISTS trigger_validate_completed_interview ON interviews;
CREATE TRIGGER trigger_validate_completed_interview
    BEFORE INSERT OR UPDATE ON interviews
    FOR EACH ROW
    EXECUTE FUNCTION validate_completed_interview();

-- Permisos (opcional, según la configuración de seguridad)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON interviews TO admin;
-- GRANT SELECT, INSERT, UPDATE ON interviews TO teachers;
-- GRANT SELECT ON interviews TO readonly_users;

COMMIT;