# API Documentation - Sistema de Usuarios del Colegio

## Base URL
```
http://localhost:8080/api/school-users
```

## Autenticación
Todas las rutas requieren autenticación con role ADMIN. Se debe incluir el token JWT en el header:
```
Authorization: Bearer <token>
```

## Endpoints

### 1. Crear Usuario del Colegio
**POST** `/api/school-users`

Crea un nuevo usuario del personal del colegio (profesor, personal kinder, psicólogo o personal de apoyo).

**Request Body:**
```json
{
  "firstName": "string",
  "lastName": "string", 
  "email": "string (debe terminar en @mtn.cl)",
  "password": "string (mínimo 6 caracteres)",
  "role": "PROFESSOR | KINDER_TEACHER | PSYCHOLOGIST | SUPPORT_STAFF",
  "phone": "string (opcional)",
  
  // Campos específicos para PROFESSOR
  "subjects": ["MATH", "SPANISH", "ENGLISH"],
  "assignedGrades": ["prekinder", "kinder", "1basico", ...],
  "department": "string",
  "yearsOfExperience": "integer",
  "qualifications": ["string"],
  
  // Campos específicos para KINDER_TEACHER
  "assignedLevel": "PREKINDER | KINDER",
  "specializations": ["Desarrollo Motor", "Lenguaje Inicial", ...],
  
  // Campos específicos para PSYCHOLOGIST
  "specialty": "EDUCATIONAL | CLINICAL | DEVELOPMENTAL | COGNITIVE",
  "licenseNumber": "string",
  "canConductInterviews": "boolean",
  "canPerformPsychologicalEvaluations": "boolean",
  "specializedAreas": ["string"],
  
  // Campos específicos para SUPPORT_STAFF
  "staffType": "ADMINISTRATIVE | TECHNICAL | ACADEMIC_COORDINATOR | STUDENT_SERVICES | IT_SUPPORT",
  "responsibilities": ["string"],
  "canAccessReports": "boolean",
  "canManageSchedules": "boolean"
}
```

**Response:**
```json
{
  "id": "long",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "role": "string",
  "phone": "string",
  "isActive": "boolean",
  "fechaRegistro": "datetime",
  "updatedAt": "datetime",
  // + campos específicos del rol
}
```

### 2. Obtener Todos los Usuarios
**GET** `/api/school-users`

Retorna todos los usuarios del personal del colegio.

**Response:** Array de objetos usuario

### 3. Obtener Usuarios Activos
**GET** `/api/school-users/active`

Retorna solo los usuarios activos del personal del colegio.

### 4. Obtener Usuarios por Rol
**GET** `/api/school-users/by-role/{role}`

**Path Parameters:**
- `role`: PROFESSOR | KINDER_TEACHER | PSYCHOLOGIST | SUPPORT_STAFF

### 5. Obtener Usuario por ID
**GET** `/api/school-users/{id}`

**Path Parameters:**
- `id`: ID del usuario

### 6. Actualizar Usuario
**PUT** `/api/school-users/{id}`

Actualiza los datos de un usuario existente.

**Request Body:** Mismo formato que crear usuario (sin password y email)

### 7. Desactivar Usuario
**PATCH** `/api/school-users/{id}/deactivate`

Desactiva un usuario (no lo elimina, solo cambia isActive a false).

**Response:**
```json
{
  "message": "Usuario desactivado exitosamente"
}
```

### 8. Reactivar Usuario
**PATCH** `/api/school-users/{id}/reactivate`

Reactiva un usuario desactivado.

### 9. Endpoints Específicos por Rol

**GET** `/api/school-users/professors` - Solo profesores
**GET** `/api/school-users/kinder-teachers` - Solo personal kinder
**GET** `/api/school-users/psychologists` - Solo psicólogos
**GET** `/api/school-users/support-staff` - Solo personal de apoyo

### 10. Estadísticas
**GET** `/api/school-users/stats`

Retorna estadísticas del personal del colegio.

**Response:**
```json
{
  "totalUsers": "number",
  "activeUsers": "number", 
  "inactiveUsers": "number",
  "professors": "number",
  "kinderTeachers": "number",
  "psychologists": "number",
  "supportStaff": "number"
}
```

## Códigos de Error

- **400 Bad Request**: Datos de entrada inválidos
- **401 Unauthorized**: Token de autenticación faltante o inválido
- **403 Forbidden**: Sin permisos para acceder al recurso
- **404 Not Found**: Usuario no encontrado
- **409 Conflict**: Email ya existe en el sistema
- **500 Internal Server Error**: Error del servidor

## Ejemplos de Uso

### Crear un Profesor de Matemática
```bash
curl -X POST http://localhost:8080/api/school-users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "María Elena",
    "lastName": "González",
    "email": "maria.gonzalez@mtn.cl",
    "password": "profesor123",
    "role": "PROFESSOR",
    "phone": "+56912345678",
    "subjects": ["MATH"],
    "assignedGrades": ["prekinder", "kinder", "1basico", "2basico"],
    "department": "Matemática Inicial",
    "yearsOfExperience": 5,
    "qualifications": ["Educación Parvularia", "Mención Matemática"]
  }'
```

### Crear Personal de Kinder
```bash
curl -X POST http://localhost:8080/api/school-users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "Ana Patricia",
    "lastName": "López",
    "email": "ana.lopez@mtn.cl", 
    "password": "kinder123",
    "role": "KINDER_TEACHER",
    "assignedLevel": "PREKINDER",
    "specializations": ["Desarrollo Motor", "Juego Educativo"],
    "yearsOfExperience": 3
  }'
```

### Crear Psicólogo
```bash
curl -X POST http://localhost:8080/api/school-users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "Carlos Roberto",
    "lastName": "Ruiz",
    "email": "carlos.ruiz@mtn.cl",
    "password": "psi123",
    "role": "PSYCHOLOGIST", 
    "specialty": "EDUCATIONAL",
    "licenseNumber": "PSI-12345",
    "assignedGrades": ["prekinder", "kinder", "1basico"],
    "canConductInterviews": true,
    "canPerformPsychologicalEvaluations": true,
    "specializedAreas": ["Dificultades de Aprendizaje"]
  }'
```

## Notas Importantes

1. **Emails**: Deben terminar en `@mtn.cl` para personal del colegio
2. **Roles**: Solo se permiten los 4 roles específicos del colegio
3. **Profesores**: Solo pueden enseñar MATH, SPANISH o ENGLISH
4. **Personal Kinder**: Solo puede ser asignado a PREKINDER o KINDER
5. **Validaciones**: Todos los campos requeridos deben estar presentes según el rol
6. **Eliminación**: Los usuarios no se eliminan, solo se desactivan
7. **Unicidad**: Los emails deben ser únicos en todo el sistema