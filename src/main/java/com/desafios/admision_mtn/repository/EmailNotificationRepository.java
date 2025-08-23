package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.EmailNotification;
import com.desafios.admision_mtn.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
    
    // Buscar por token de tracking
    Optional<EmailNotification> findByTrackingToken(String trackingToken);
    
    // Buscar por token de respuesta
    Optional<EmailNotification> findByResponseToken(String responseToken);
    
    // Buscar todas las notificaciones de una aplicación
    List<EmailNotification> findByApplicationOrderByCreatedAtDesc(Application application);
    
    // Buscar por ID de aplicación
    List<EmailNotification> findByApplication_IdOrderByCreatedAtDesc(Long applicationId);
    
    // Buscar por tipo de email
    List<EmailNotification> findByEmailTypeOrderByCreatedAtDesc(EmailNotification.EmailType emailType);
    
    // Buscar emails no abiertos
    @Query("SELECT en FROM EmailNotification en WHERE en.opened = false ORDER BY en.createdAt DESC")
    List<EmailNotification> findUnopened();
    
    // Buscar emails que requieren respuesta y no han sido respondidos
    @Query("SELECT en FROM EmailNotification en WHERE en.responseRequired = true AND en.responded = false ORDER BY en.createdAt DESC")
    List<EmailNotification> findPendingResponses();
    
    // Buscar emails enviados en un rango de fechas
    List<EmailNotification> findBySentAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Estadísticas de emails por tipo
    @Query("SELECT en.emailType, COUNT(en) FROM EmailNotification en GROUP BY en.emailType")
    List<Object[]> getEmailStatsByType();
    
    // Estadísticas de apertura
    @Query("SELECT " +
           "COUNT(en) as total, " +
           "SUM(CASE WHEN en.opened = true THEN 1 ELSE 0 END) as opened, " +
           "SUM(CASE WHEN en.responded = true THEN 1 ELSE 0 END) as responded " +
           "FROM EmailNotification en")
    Object[] getEmailStats();
    
    // Buscar emails por recipient email
    List<EmailNotification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    // Buscar emails de una escuela específica
    List<EmailNotification> findByTargetSchoolOrderByCreatedAtDesc(EmailNotification.TargetSchool targetSchool);
    
    // Buscar emails recientes (últimos 30 días)
    @Query("SELECT en FROM EmailNotification en WHERE en.createdAt >= :thirtyDaysAgo ORDER BY en.createdAt DESC")
    List<EmailNotification> findRecentEmails(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    // Contar emails no leídos por aplicación
    @Query("SELECT COUNT(en) FROM EmailNotification en WHERE en.application.id = :applicationId AND en.opened = false")
    Long countUnreadByApplicationId(@Param("applicationId") Long applicationId);
    
    // Buscar último email enviado por tipo para una aplicación
    @Query("SELECT en FROM EmailNotification en WHERE en.application.id = :applicationId AND en.emailType = :emailType ORDER BY en.createdAt DESC LIMIT 1")
    Optional<EmailNotification> findLastEmailByApplicationAndType(@Param("applicationId") Long applicationId, @Param("emailType") EmailNotification.EmailType emailType);
    
    // Buscar emails con paginación
    Page<EmailNotification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Buscar emails por estado de apertura con paginación
    Page<EmailNotification> findByOpenedOrderByCreatedAtDesc(Boolean opened, Pageable pageable);
    
    // Buscar emails que necesitan respuesta con paginación
    Page<EmailNotification> findByResponseRequiredOrderByCreatedAtDesc(Boolean responseRequired, Pageable pageable);
}