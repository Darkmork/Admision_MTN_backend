package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.EmailEvent;
import com.desafios.admision_mtn.entity.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, Long> {
    
    // Buscar eventos por notificación
    List<EmailEvent> findByEmailNotificationOrderByCreatedAtDesc(EmailNotification emailNotification);
    
    // Buscar eventos por tipo
    List<EmailEvent> findByEventTypeOrderByCreatedAtDesc(EmailEvent.EventType eventType);
    
    // Buscar eventos en rango de fechas
    List<EmailEvent> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Contar eventos por tipo para una notificación
    @Query("SELECT COUNT(ee) FROM EmailEvent ee WHERE ee.emailNotification.id = :notificationId AND ee.eventType = :eventType")
    Long countEventsByNotificationAndType(@Param("notificationId") Long notificationId, @Param("eventType") EmailEvent.EventType eventType);
    
    // Estadísticas de eventos por tipo
    @Query("SELECT ee.eventType, COUNT(ee) FROM EmailEvent ee GROUP BY ee.eventType")
    List<Object[]> getEventStatsByType();
    
    // Buscar último evento de un tipo específico para una notificación
    @Query("SELECT ee FROM EmailEvent ee WHERE ee.emailNotification.id = :notificationId AND ee.eventType = :eventType ORDER BY ee.createdAt DESC LIMIT 1")
    EmailEvent findLastEventByNotificationAndType(@Param("notificationId") Long notificationId, @Param("eventType") EmailEvent.EventType eventType);
}