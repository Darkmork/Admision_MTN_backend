package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.EmailQueueService;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/institutional-emails")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class InstitutionalEmailController {

    @Autowired
    private EmailQueueService emailQueueService;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private InterviewRepository interviewRepository;

    /**
     * Enviar email de aplicación recibida
     */
    @PostMapping("/application-received/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendApplicationReceivedEmail(@PathVariable Long applicationId) {
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Aplicación no encontrada"
            ));
        }
        
        String queueId = emailQueueService.queueApplicationReceivedEmail(applicationOpt.get());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email de aplicación recibida agregado a cola institucional",
            "queueId", queueId
        ));
    }

    /**
     * Enviar invitación a entrevista
     */
    @PostMapping("/interview-invitation/{interviewId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<Map<String, Object>> sendInterviewInvitationEmail(@PathVariable Long interviewId) {
        Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
        if (interviewOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Entrevista no encontrada"
            ));
        }
        
        Interview interview = interviewOpt.get();
        String queueId = emailQueueService.queueInterviewInvitationEmail(interview.getApplication(), interview);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Invitación a entrevista agregada a cola institucional",
            "queueId", queueId
        ));
    }

    /**
     * Enviar actualización de estado
     */
    @PostMapping("/status-update/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendStatusUpdateEmail(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Aplicación no encontrada"
            ));
        }
        
        String newStatus = request.get("newStatus");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Estado requerido"
            ));
        }
        
        String queueId = emailQueueService.queueStatusUpdateEmail(applicationOpt.get(), newStatus);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email de actualización de estado agregado a cola institucional",
            "queueId", queueId
        ));
    }

    /**
     * Enviar recordatorio de documentos
     */
    @PostMapping("/document-reminder/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendDocumentReminderEmail(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Aplicación no encontrada"
            ));
        }
        
        String pendingDocuments = request.get("pendingDocuments");
        if (pendingDocuments == null || pendingDocuments.trim().isEmpty()) {
            pendingDocuments = "Documentos requeridos para completar la postulación";
        }
        
        String queueId = emailQueueService.queueDocumentReminderEmail(applicationOpt.get(), pendingDocuments);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Recordatorio de documentos agregado a cola institucional",
            "queueId", queueId
        ));
    }

    /**
     * Enviar resultado de admisión
     */
    @PostMapping("/admission-result/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendAdmissionResultEmail(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Aplicación no encontrada"
            ));
        }
        
        String result = request.get("result");
        String message = request.getOrDefault("message", "");
        
        if (result == null || result.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Resultado requerido"
            ));
        }
        
        String queueId = emailQueueService.queueAdmissionResultEmail(applicationOpt.get(), result, message);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email de resultado de admisión agregado a cola institucional",
            "queueId", queueId
        ));
    }

    /**
     * Estadísticas de la cola institucional
     */
    @GetMapping("/queue/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getQueueStatistics() {
        Map<String, Object> stats = emailQueueService.getQueueStatistics();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", stats
        ));
    }

    /**
     * Forzar procesamiento de cola (para testing)
     */
    @PostMapping("/queue/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> forceProcessQueue() {
        emailQueueService.forceProcessQueue();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Procesamiento de cola forzado"
        ));
    }

    /**
     * Limpiar cola (emergencia)
     */
    @DeleteMapping("/queue/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearQueue() {
        emailQueueService.clearQueue();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cola de emails limpiada"
        ));
    }

    /**
     * Enviar email de prueba institucional
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email requerido"
            ));
        }

        // Para testing, usar la primera aplicación disponible
        Optional<Application> testApp = applicationRepository.findAll().stream().findFirst();
        if (testApp.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "No hay aplicaciones disponibles para prueba"
            ));
        }

        // Temporalmente cambiar el email del padre para la prueba
        String originalEmail = testApp.get().getFather().getEmail();
        testApp.get().getFather().setEmail(email);
        
        String queueId = emailQueueService.queueApplicationReceivedEmail(testApp.get());
        
        // Restaurar email original
        testApp.get().getFather().setEmail(originalEmail);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email de prueba institucional agregado a cola",
            "queueId", queueId
        ));
    }
}