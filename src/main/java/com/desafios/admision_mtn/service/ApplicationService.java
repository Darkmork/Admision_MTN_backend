package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.ApplicationResponse;
import com.desafios.admision_mtn.dto.CreateApplicationRequest;
import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public ApplicationResponse createApplication(CreateApplicationRequest request, String userEmail) {
        try {
            log.info("Creating application for user: {}", userEmail);
            
            // Buscar el usuario que está creando la postulación
            User applicantUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar si ya existe una postulación para este RUT
            if (applicationRepository.findByStudentRut(request.getRut()).isPresent()) {
                throw new RuntimeException("Ya existe una postulación para este RUT");
            }

            // Crear entidad Student
            Student student = new Student();
            student.setFirstName(toUpperCase(request.getFirstName()));
            student.setLastName(toUpperCase(request.getLastName()));
            student.setMaternalLastName(toUpperCase(request.getMaternalLastName()));
            student.setRut(request.getRut());
            student.setBirthDate(LocalDate.parse(request.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            student.setEmail(request.getStudentEmail());
            student.setAddress(toUpperCase(request.getStudentAddress()));
            student.setGradeApplied(request.getGrade());
            student.setSchoolApplied(request.getSchoolApplied());
            student.setCurrentSchool(toUpperCase(request.getCurrentSchool()));
            student.setAdditionalNotes(toUpperCase(request.getAdditionalNotes()));

            // Crear entidad Parent (Padre)
            Parent father = new Parent();
            father.setFullName(toUpperCase(request.getParent1Name()));
            father.setRut(request.getParent1Rut());
            father.setEmail(request.getParent1Email());
            father.setPhone(request.getParent1Phone());
            father.setAddress(toUpperCase(request.getParent1Address()));
            father.setProfession(toUpperCase(request.getParent1Profession()));
            father.setParentType(Parent.ParentType.FATHER);

            // Crear entidad Parent (Madre)
            Parent mother = new Parent();
            mother.setFullName(toUpperCase(request.getParent2Name()));
            mother.setRut(request.getParent2Rut());
            mother.setEmail(request.getParent2Email());
            mother.setPhone(request.getParent2Phone());
            mother.setAddress(toUpperCase(request.getParent2Address()));
            mother.setProfession(toUpperCase(request.getParent2Profession()));
            mother.setParentType(Parent.ParentType.MOTHER);

            // Crear entidad Supporter
            Supporter supporter = new Supporter();
            supporter.setFullName(toUpperCase(request.getSupporterName()));
            supporter.setRut(request.getSupporterRut());
            supporter.setEmail(request.getSupporterEmail());
            supporter.setPhone(request.getSupporterPhone());
            supporter.setRelationship(parseRelationship(toUpperCase(request.getSupporterRelation())));

            // Crear entidad Guardian
            Guardian guardian = new Guardian();
            guardian.setFullName(toUpperCase(request.getGuardianName()));
            guardian.setRut(request.getGuardianRut());
            guardian.setEmail(request.getGuardianEmail());
            guardian.setPhone(request.getGuardianPhone());
            guardian.setRelationship(parseGuardianRelationship(toUpperCase(request.getGuardianRelation())));

            // Crear entidad Application
            Application application = new Application();
            application.setStudent(student);
            application.setFather(father);
            application.setMother(mother);
            application.setSupporter(supporter);
            application.setGuardian(guardian);
            application.setApplicantUser(applicantUser);
            application.setStatus(Application.ApplicationStatus.PENDING);
            application.setSubmissionDate(LocalDateTime.now());
            application.setAdditionalNotes(request.getAdditionalNotes());

            // Establecer relaciones bidireccionales
            student.setApplication(application);
            father.setApplicationAsFather(application);
            mother.setApplicationAsMother(application);
            supporter.setApplication(application);
            guardian.setApplication(application);

            // Guardar la aplicación (cascada guardará todas las entidades relacionadas)
            Application savedApplication = applicationRepository.save(application);

            log.info("Application created successfully with ID: {}", savedApplication.getId());
            return ApplicationResponse.success(savedApplication);

        } catch (Exception e) {
            log.error("Error creating application for user: {}", userEmail, e);
            throw new RuntimeException("Error al crear la postulación: " + e.getMessage());
        }
    }

    public List<Application> getApplicationsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return applicationRepository.findByApplicantUserWithStudent(user);
    }

    public List<Application> getAllApplications() {
        try {
            log.info("Attempting to fetch all applications...");
            
            // TEMPORAL: Usar JDBC directo para esquivar problemas de Hibernate
            try {
                Integer jdbcCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications", Integer.class);
                log.info("JdbcTemplate finds {} applications in database", jdbcCount);
                
                if (jdbcCount > 0) {
                    // Si hay datos, usar consulta JDBC para obtener los datos básicos
                    List<Map<String, Object>> rawData = jdbcTemplate.queryForList(
                        "SELECT a.id, a.status, a.submission_date, a.created_at, " +
                        "s.first_name, s.paternal_last_name, s.maternal_last_name, s.rut " +
                        "FROM applications a " +
                        "LEFT JOIN students s ON a.student_id = s.id " +
                        "ORDER BY a.created_at DESC LIMIT 50"
                    );
                    
                    log.info("Retrieved {} application records using JDBC", rawData.size());
                    
                    // Convertir a objetos Application básicos (sin relaciones completas)
                    List<Application> applications = rawData.stream().map(row -> {
                        Application app = new Application();
                        app.setId(((Number) row.get("id")).longValue());
                        app.setStatus(Application.ApplicationStatus.valueOf((String) row.get("status")));
                        
                        // Crear un objeto Student básico
                        Student student = new Student();
                        student.setFirstName((String) row.get("first_name"));
                        student.setLastName((String) row.get("paternal_last_name"));
                        student.setMaternalLastName((String) row.get("maternal_last_name"));
                        student.setRut((String) row.get("rut"));
                        app.setStudent(student);
                        
                        return app;
                    }).collect(Collectors.toList());
                    
                    log.info("Successfully converted {} JDBC records to Application objects", applications.size());
                    return applications;
                }
            } catch (Exception jdbcEx) {
                log.error("JDBC query failed: {}", jdbcEx.getMessage(), jdbcEx);
            }
            
            // Fallback al método JPA original
            List<Application> applications = applicationRepository.findAll();
            log.info("JPA Repository found {} applications", applications.size());
            
            return applications;
            
        } catch (Exception e) {
            log.error("Error fetching applications: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
    }

    @Transactional
    public Application updateApplicationStatus(Long id, Application.ApplicationStatus status) {
        Application application = getApplicationById(id);
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    private Supporter.Relationship parseRelationship(String relationship) {
        try {
            return Supporter.Relationship.valueOf(relationship.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid supporter relationship: {}, defaulting to OTRO", relationship);
            return Supporter.Relationship.OTRO;
        }
    }

    private Guardian.Relationship parseGuardianRelationship(String relationship) {
        try {
            return Guardian.Relationship.valueOf(relationship.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid guardian relationship: {}, defaulting to OTRO", relationship);
            return Guardian.Relationship.OTRO;
        }
    }

    /**
     * Obtiene los tipos de documentos requeridos para una aplicación
     */
    public List<Document.DocumentType> getRequiredDocumentTypes() {
        return Arrays.asList(
            Document.DocumentType.BIRTH_CERTIFICATE,
            Document.DocumentType.GRADES_2023,
            Document.DocumentType.GRADES_2024,
            Document.DocumentType.GRADES_2025_SEMESTER_1,
            Document.DocumentType.PERSONALITY_REPORT_2024,
            Document.DocumentType.PERSONALITY_REPORT_2025_SEMESTER_1
        );
    }

    /**
     * Obtiene los tipos de documentos faltantes para una aplicación
     */
    public List<Document.DocumentType> getMissingDocuments(Long applicationId) {
        Application application = getApplicationById(applicationId);
        
        // Obtener documentos existentes de la aplicación
        Set<Document.DocumentType> existingTypes = application.getDocuments().stream()
                .map(Document::getDocumentType)
                .collect(Collectors.toSet());
        
        // Filtrar documentos requeridos que no están presentes
        return getRequiredDocumentTypes().stream()
                .filter(type -> !existingTypes.contains(type))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si una aplicación tiene todos los documentos requeridos
     */
    public boolean hasAllRequiredDocuments(Long applicationId) {
        return getMissingDocuments(applicationId).isEmpty();
    }

    /**
     * Actualiza el estado de la aplicación basado en los documentos completados
     */
    @Transactional
    public Application updateApplicationStatusBasedOnDocuments(Long applicationId) {
        Application application = getApplicationById(applicationId);
        
        if (hasAllRequiredDocuments(applicationId)) {
            // Si tiene todos los documentos, cambiar a PENDING
            if (application.getStatus() == Application.ApplicationStatus.DOCUMENTS_REQUESTED) {
                application.setStatus(Application.ApplicationStatus.PENDING);
                log.info("Application {} status updated to PENDING - all documents completed", applicationId);
            }
        } else {
            // Si faltan documentos, cambiar a DOCUMENTS_REQUESTED
            if (application.getStatus() == Application.ApplicationStatus.PENDING) {
                application.setStatus(Application.ApplicationStatus.DOCUMENTS_REQUESTED);
                log.info("Application {} status updated to DOCUMENTS_REQUESTED - missing documents", applicationId);
            }
        }
        
        return applicationRepository.save(application);
    }

    /**
     * Obtiene información completa sobre el estado de documentos de una aplicación
     */
    public Map<String, Object> getApplicationDocumentStatus(Long applicationId) {
        Application application = getApplicationById(applicationId);
        List<Document.DocumentType> requiredTypes = getRequiredDocumentTypes();
        List<Document.DocumentType> missingTypes = getMissingDocuments(applicationId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("applicationId", applicationId);
        status.put("status", application.getStatus());
        status.put("totalRequiredDocuments", requiredTypes.size());
        status.put("uploadedDocuments", requiredTypes.size() - missingTypes.size());
        status.put("missingDocuments", missingTypes);
        status.put("isComplete", missingTypes.isEmpty());
        status.put("completionPercentage", 
            Math.round(((double) (requiredTypes.size() - missingTypes.size()) / requiredTypes.size()) * 100));
        
        return status;
    }

    /**
     * Limpia completamente la base de datos (solo para desarrollo)
     */
    @Transactional
    public void deleteAllData() {
        log.warn("DELETING ALL DATA FROM DATABASE - Development only");
        
        try {
            // Eliminar en orden correcto para evitar problemas de FK
            
            // 1. Eliminar evaluaciones (referencian applications)
            evaluationRepository.deleteAll();
            
            // 2. Eliminar documentos (referencian applications)  
            applicationRepository.findAll().forEach(app -> {
                if (app.getDocuments() != null) {
                    app.getDocuments().clear();
                }
            });
            
            // 3. Eliminar applications (referencian student, parents, etc.)
            applicationRepository.deleteAll();
            
            // 4. Eliminar usuarios
            userRepository.deleteAll();
            
            log.info("All data deleted successfully");
            
        } catch (Exception e) {
            log.error("Error deleting all data", e);
            throw new RuntimeException("Error limpiando la base de datos: " + e.getMessage());
        }
    }

    /**
     * Convierte texto a mayúsculas, manejando valores nulos
     */
    private String toUpperCase(String text) {
        return text != null ? text.trim().toUpperCase() : null;
    }
}