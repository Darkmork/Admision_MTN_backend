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
            student.setFirstName(request.getFirstName());
            student.setLastName(request.getLastName());
            student.setRut(request.getRut());
            student.setBirthDate(LocalDate.parse(request.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            student.setEmail(request.getStudentEmail());
            student.setAddress(request.getStudentAddress());
            student.setGradeApplied(request.getGrade());
            student.setCurrentSchool(request.getCurrentSchool());
            student.setAdditionalNotes(request.getAdditionalNotes());

            // Crear entidad Parent (Padre)
            Parent father = new Parent();
            father.setFullName(request.getParent1Name());
            father.setRut(request.getParent1Rut());
            father.setEmail(request.getParent1Email());
            father.setPhone(request.getParent1Phone());
            father.setAddress(request.getParent1Address());
            father.setProfession(request.getParent1Profession());
            father.setParentType(Parent.ParentType.FATHER);

            // Crear entidad Parent (Madre)
            Parent mother = new Parent();
            mother.setFullName(request.getParent2Name());
            mother.setRut(request.getParent2Rut());
            mother.setEmail(request.getParent2Email());
            mother.setPhone(request.getParent2Phone());
            mother.setAddress(request.getParent2Address());
            mother.setProfession(request.getParent2Profession());
            mother.setParentType(Parent.ParentType.MOTHER);

            // Crear entidad Supporter
            Supporter supporter = new Supporter();
            supporter.setFullName(request.getSupporterName());
            supporter.setRut(request.getSupporterRut());
            supporter.setEmail(request.getSupporterEmail());
            supporter.setPhone(request.getSupporterPhone());
            supporter.setRelationship(parseRelationship(request.getSupporterRelation()));

            // Crear entidad Guardian
            Guardian guardian = new Guardian();
            guardian.setFullName(request.getGuardianName());
            guardian.setRut(request.getGuardianRut());
            guardian.setEmail(request.getGuardianEmail());
            guardian.setPhone(request.getGuardianPhone());
            guardian.setRelationship(parseGuardianRelationship(request.getGuardianRelation()));

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
        return applicationRepository.findAllWithRelations();
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
}