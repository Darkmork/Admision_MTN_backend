-- Script para crear entrevistas de muestra
-- Ejecutar con: PGPASSWORD=admin123 /opt/homebrew/Cellar/postgresql@15/15.13/bin/psql -h localhost -U admin -d "Admisión_MTN_DB" -f "create_sample_interviews.sql"

-- Primero verificamos que existan applications y users
DO $$
DECLARE
    app_count INTEGER;
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO app_count FROM applications;
    SELECT COUNT(*) INTO user_count FROM users WHERE role IN ('ADMIN', 'CYCLE_DIRECTOR', 'TEACHER_LANGUAGE', 'TEACHER_MATHEMATICS', 'TEACHER_ENGLISH', 'PSYCHOLOGIST');
    
    IF app_count = 0 THEN
        RAISE NOTICE 'No se encontraron aplicaciones. Necesitas crear applications antes de insertar entrevistas.';
    END IF;
    
    IF user_count = 0 THEN
        RAISE NOTICE 'No se encontraron usuarios entrevistadores. Necesitas crear users antes de insertar entrevistas.';
    END IF;
    
    RAISE NOTICE 'Aplicaciones encontradas: %, Usuarios entrevistadores: %', app_count, user_count;
END $$;

-- Limpiar entrevistas existentes (opcional)
-- DELETE FROM interviews;

-- Insertar entrevistas de muestra (solo si existen applications y users)
DO $$
DECLARE
    app_ids BIGINT[];
    interviewer_ids BIGINT[];
    sample_app_id BIGINT;
    sample_interviewer_id BIGINT;
BEGIN
    -- Obtener IDs de applications existentes
    SELECT ARRAY(SELECT id FROM applications LIMIT 10) INTO app_ids;
    
    -- Obtener IDs de usuarios que pueden ser entrevistadores
    SELECT ARRAY(SELECT id FROM users WHERE role IN ('ADMIN', 'CYCLE_DIRECTOR', 'TEACHER_LANGUAGE', 'TEACHER_MATHEMATICS', 'TEACHER_ENGLISH', 'PSYCHOLOGIST') LIMIT 5) INTO interviewer_ids;
    
    -- Solo insertar si tenemos datos
    IF array_length(app_ids, 1) > 0 AND array_length(interviewer_ids, 1) > 0 THEN
        
        -- Entrevista 1: Individual programada para mañana
        sample_app_id := app_ids[1];
        sample_interviewer_id := interviewer_ids[1];
        
        INSERT INTO interviews (
            application_id, interviewer_user_id, status, type, mode,
            scheduled_date, scheduled_time, duration, location, notes, preparation
        ) VALUES (
            sample_app_id, sample_interviewer_id, 'SCHEDULED', 'INDIVIDUAL', 'IN_PERSON',
            CURRENT_DATE + INTERVAL '1 day', '09:00:00', 60, 
            'Sala de entrevistas - Edificio principal', 
            'Primera entrevista individual con el estudiante.',
            'Revisar expediente académico y preparar preguntas sobre motivación.'
        );
        
        -- Entrevista 2: Familiar confirmada para hoy
        IF array_length(app_ids, 1) > 1 THEN
            sample_app_id := app_ids[2];
            sample_interviewer_id := interviewer_ids[CASE WHEN array_length(interviewer_ids, 1) > 1 THEN 2 ELSE 1 END];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, virtual_meeting_link, notes, preparation
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'CONFIRMED', 'FAMILY', 'VIRTUAL',
                CURRENT_DATE, '15:00:00', 90,
                'https://meet.google.com/abc-defg-hij',
                'Entrevista familiar virtual. Padres y estudiante juntos.',
                'Preparar preguntas sobre dinámicas familiares y expectativas.'
            );
        END IF;
        
        -- Entrevista 3: Psicológica completada
        IF array_length(app_ids, 1) > 2 THEN
            sample_app_id := app_ids[3];
            sample_interviewer_id := interviewer_ids[CASE WHEN array_length(interviewer_ids, 1) > 2 THEN 3 ELSE 1 END];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, location, 
                notes, preparation, result, score, recommendations, follow_up_required, completed_at
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'COMPLETED', 'PSYCHOLOGICAL', 'IN_PERSON',
                CURRENT_DATE - INTERVAL '2 days', '10:30:00', 120,
                'Consultorio psicológico - Piso 2',
                'Evaluación psicológica completa realizada.',
                'Aplicar tests de personalidad y habilidades sociales.',
                'POSITIVE', 8.5, 
                'Estudiante demuestra excelente estabilidad emocional y habilidades de adaptación. Recomendado para admisión.',
                false, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '2 hours'
            );
        END IF;
        
        -- Entrevista 4: Académica en progreso
        IF array_length(app_ids, 1) > 3 THEN
            sample_app_id := app_ids[4];
            sample_interviewer_id := interviewer_ids[CASE WHEN array_length(interviewer_ids, 1) > 3 THEN 4 ELSE 1 END];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, location, notes, preparation
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'IN_PROGRESS', 'ACADEMIC', 'IN_PERSON',
                CURRENT_DATE, '11:00:00', 75,
                'Aula de matemáticas - Edificio académico',
                'Evaluación de conocimientos matemáticos en curso.',
                'Preparar ejercicios de álgebra y geometría acordes al nivel.'
            );
        END IF;
        
        -- Entrevista 5: Comportamental cancelada
        IF array_length(app_ids, 1) > 4 THEN
            sample_app_id := app_ids[5];
            sample_interviewer_id := interviewer_ids[CASE WHEN array_length(interviewer_ids, 1) > 4 THEN 5 ELSE 1 END];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, notes
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'CANCELLED', 'BEHAVIORAL', 'IN_PERSON',
                CURRENT_DATE - INTERVAL '1 day', '14:00:00', 60,
                'Entrevista cancelada por enfermedad del estudiante.'
            );
        END IF;
        
        -- Entrevista 6: Individual completada con seguimiento requerido
        IF array_length(app_ids, 1) > 5 THEN
            sample_app_id := app_ids[6];
            sample_interviewer_id := interviewer_ids[1];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, location,
                notes, preparation, result, score, recommendations, 
                follow_up_required, follow_up_notes, completed_at
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'COMPLETED', 'INDIVIDUAL', 'IN_PERSON',
                CURRENT_DATE - INTERVAL '5 days', '16:00:00', 45,
                'Sala de entrevistas - Edificio principal',
                'Segunda entrevista individual realizada.',
                'Profundizar en áreas de interés del estudiante.',
                'REQUIRES_FOLLOW_UP', 7.0,
                'Estudiante muestra potencial pero necesita mayor orientación vocacional.',
                true, 'Programar sesión de orientación vocacional en 2 semanas.',
                CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '45 minutes'
            );
        END IF;
        
        -- Entrevista 7: Programada para la próxima semana (virtual)
        IF array_length(app_ids, 1) > 6 THEN
            sample_app_id := app_ids[7];
            sample_interviewer_id := interviewer_ids[CASE WHEN array_length(interviewer_ids, 1) > 1 THEN 2 ELSE 1 END];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, virtual_meeting_link, notes, preparation
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'SCHEDULED', 'FAMILY', 'VIRTUAL',
                CURRENT_DATE + INTERVAL '7 days', '10:00:00', 90,
                'https://zoom.us/j/123456789',
                'Entrevista familiar programada para la próxima semana.',
                'Revisar antecedentes familiares y preparar ambiente virtual.'
            );
        END IF;
        
        -- Entrevista 8: No show (estudiante no asistió)
        IF array_length(app_ids, 1) > 7 THEN
            sample_app_id := app_ids[8];
            sample_interviewer_id := interviewer_ids[CASE WHEN array_length(interviewer_ids, 1) > 2 THEN 3 ELSE 1 END];
            
            INSERT INTO interviews (
                application_id, interviewer_user_id, status, type, mode,
                scheduled_date, scheduled_time, duration, location, notes
            ) VALUES (
                sample_app_id, sample_interviewer_id, 'NO_SHOW', 'INDIVIDUAL', 'IN_PERSON',
                CURRENT_DATE - INTERVAL '3 days', '13:30:00', 60,
                'Sala de entrevistas - Edificio principal',
                'Estudiante no se presentó a la entrevista programada.'
            );
        END IF;
        
        RAISE NOTICE 'Entrevistas de muestra creadas exitosamente.';
    ELSE
        RAISE NOTICE 'No se pudieron crear entrevistas: faltan applications o usuarios entrevistadores.';
    END IF;
END $$;

-- Verificar las entrevistas creadas
SELECT 
    i.id,
    i.status,
    i.type,
    i.mode,
    i.scheduled_date,
    i.scheduled_time,
    i.duration,
    COALESCE(i.result, 'N/A') as result,
    i.score,
    u.first_name || ' ' || u.last_name as interviewer_name,
    s.first_name || ' ' || s.last_name as student_name
FROM interviews i
JOIN users u ON i.interviewer_user_id = u.id
JOIN applications a ON i.application_id = a.id
JOIN students s ON a.student_id = s.id
ORDER BY i.scheduled_date DESC, i.scheduled_time DESC;

COMMIT;