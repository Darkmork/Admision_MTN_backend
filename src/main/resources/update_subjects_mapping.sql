-- Script para actualizar las materias existentes en la base de datos
-- Este script convierte los nombres en español a los valores del enum del backend

-- Actualizar materias de profesores existentes
-- Convertir nombres en español a valores del enum

-- Matemática -> MATH
UPDATE professor_subjects 
SET subject = 'MATH' 
WHERE subject IN ('Matemática', 'matematica', 'MATEMATICA', 'Matematica');

-- Lenguaje -> SPANISH  
UPDATE professor_subjects 
SET subject = 'SPANISH' 
WHERE subject IN ('Lenguaje', 'lenguaje', 'LENGUAJE', 'Lengua', 'lengua', 'LENGUA');

-- Inglés -> ENGLISH
UPDATE professor_subjects 
SET subject = 'ENGLISH' 
WHERE subject IN ('Inglés', 'ingles', 'INGLES', 'Ingles', 'English', 'english', 'ENGLISH');

-- Eliminar materias que no corresponden a los valores del enum
DELETE FROM professor_subjects 
WHERE subject NOT IN ('MATH', 'SPANISH', 'ENGLISH');

-- Verificar que solo quedan las materias válidas
SELECT DISTINCT subject FROM professor_subjects ORDER BY subject;

-- Comentario: Después de ejecutar este script, todas las materias en la base de datos
-- deberían tener los valores correctos del enum: MATH, SPANISH, ENGLISH 