package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Interview.InterviewStatus;
import com.desafios.admision_mtn.entity.Interview.InterviewType;
import com.desafios.admision_mtn.entity.Interview.InterviewMode;
import com.desafios.admision_mtn.entity.Interview.InterviewResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // Búsquedas básicas
    List<Interview> findByStatus(InterviewStatus status);
    
    List<Interview> findByType(InterviewType type);
    
    // Note: mode is @Transient field, no database query method needed
    
    List<Interview> findByInterviewerId(Long interviewerId);
    
    List<Interview> findByApplicationId(Long applicationId);
    
    // Buscar la primera entrevista de una aplicación
    Optional<Interview> findFirstByApplicationIdOrderByScheduledDateTimeAsc(Long applicationId);
    
    // Búsquedas por fecha usando CAST para extraer solo la fecha
    @Query("SELECT i FROM Interview i WHERE CAST(i.scheduledDateTime AS date) = :date")
    List<Interview> findByScheduledDate(@Param("date") LocalDate date);
    
    @Query("SELECT i FROM Interview i WHERE CAST(i.scheduledDateTime AS date) BETWEEN :startDate AND :endDate")
    List<Interview> findByScheduledDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Entrevistas del día actual
    @Query("SELECT i FROM Interview i WHERE CAST(i.scheduledDateTime AS date) = CURRENT_DATE")
    List<Interview> findTodaysInterviews();
    
    // Entrevistas próximas (próximas 24 horas)
    @Query(value = "SELECT * FROM interviews i WHERE " +
           "i.scheduled_date >= CURRENT_DATE " +
           "AND i.scheduled_date <= CURRENT_DATE + INTERVAL '1 day'", 
           nativeQuery = true)
    List<Interview> findUpcomingInterviews();
    
    // Entrevistas vencidas
    @Query("SELECT i FROM Interview i WHERE i.scheduledDateTime < CURRENT_TIMESTAMP " +
           "AND i.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Interview> findOverdueInterviews();
    
    // Entrevistas por entrevistador y fecha
    @Query("SELECT i FROM Interview i WHERE i.interviewer.id = :interviewerId " +
           "AND CAST(i.scheduledDateTime AS date) BETWEEN :startDate AND :endDate")
    List<Interview> findByInterviewerAndDateRange(
        @Param("interviewerId") Long interviewerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Búsqueda con filtros múltiples (mode excluded - it's transient)
    @Query("SELECT i FROM Interview i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:type IS NULL OR i.type = :type) AND " +
           "(:interviewerId IS NULL OR i.interviewer.id = :interviewerId) AND " +
           "(:startDate IS NULL OR CAST(i.scheduledDateTime AS date) >= :startDate) AND " +
           "(:endDate IS NULL OR CAST(i.scheduledDateTime AS date) <= :endDate)")
    Page<Interview> findWithFilters(
        @Param("status") InterviewStatus status,
        @Param("type") InterviewType type,
        @Param("interviewerId") Long interviewerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
    // Buscar por nombre de estudiante
    @Query("SELECT i FROM Interview i WHERE " +
           "LOWER(CONCAT(i.application.student.firstName, ' ', i.application.student.lastName, ' ', i.application.student.maternalLastName)) " +
           "LIKE LOWER(CONCAT('%', :studentName, '%'))")
    List<Interview> findByStudentNameContaining(@Param("studentName") String studentName);
    
    // Estadísticas de entrevistas
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.status = :status")
    long countByStatus(@Param("status") InterviewStatus status);
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.type = :type")
    long countByType(@Param("type") InterviewType type);
    
    // Note: mode is @Transient field, no count query needed
    
    // Note: result is @Transient field, no count queries needed
    
    // Note: score is @Transient field, no average score queries needed
    
    // Estadísticas mensuales
    @Query(value = "SELECT TO_CHAR(scheduled_date, 'YYYY-MM') as month, COUNT(*) " +
           "FROM interviews " +
           "GROUP BY TO_CHAR(scheduled_date, 'YYYY-MM') " +
           "ORDER BY month", 
           nativeQuery = true)
    List<Object[]> findMonthlyStatistics();
    
    // Note: followUpRequired is @Transient field, no database query method needed
    
    // Verificar disponibilidad de horario para un entrevistador
    @Query("SELECT COUNT(i) FROM Interview i WHERE " +
           "i.interviewer.id = :interviewerId AND " +
           "i.scheduledDateTime = :dateTime AND " +
           "i.status NOT IN ('CANCELLED', 'COMPLETED')")
    long countConflictingInterviews(
        @Param("interviewerId") Long interviewerId,
        @Param("dateTime") LocalDateTime dateTime
    );
    
    // Entrevistas por rango de fechas y estado
    @Query("SELECT i FROM Interview i WHERE " +
           "CAST(i.scheduledDateTime AS date) BETWEEN :startDate AND :endDate AND " +
           "i.status IN :statuses " +
           "ORDER BY i.scheduledDateTime")
    List<Interview> findByDateRangeAndStatuses(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("statuses") List<InterviewStatus> statuses
    );
    
    // Distribución de entrevistas por estado
    @Query("SELECT i.status, COUNT(i) FROM Interview i GROUP BY i.status")
    List<Object[]> findStatusDistribution();
    
    // Distribución de entrevistas por tipo
    @Query("SELECT i.type, COUNT(i) FROM Interview i GROUP BY i.type")
    List<Object[]> findTypeDistribution();
    
    // Note: mode is @Transient field, no distribution query needed
    
    // Note: result is @Transient field, no count query needed
    
    // Buscar entrevistas por múltiples criterios con paginación
    @Query("SELECT i FROM Interview i WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(CONCAT(i.application.student.firstName, ' ', i.application.student.lastName, ' ', i.application.student.maternalLastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(i.interviewer.firstName, ' ', i.interviewer.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY i.scheduledDateTime DESC")
    Page<Interview> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Métodos adicionales para el dashboard
    @Query("SELECT i FROM Interview i WHERE CAST(i.scheduledDateTime AS date) BETWEEN :startDate AND :endDate ORDER BY i.scheduledDateTime")
    List<Interview> findUpcomingInterviews(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Note: completedAt is @Transient field, no database query method needed
    
    // Buscar entrevistas por múltiples aplicaciones (para el workflow)
    List<Interview> findByApplication_IdOrderByCreatedAtDesc(Long applicationId);
    
    // Método para cargar todas las entrevistas con relaciones simplificadas
    @Query("SELECT i FROM Interview i " +
           "LEFT JOIN FETCH i.application a " +
           "LEFT JOIN FETCH a.student s " +
           "LEFT JOIN FETCH i.interviewer u " +
           "ORDER BY i.scheduledDateTime DESC")
    Page<Interview> findAllWithRelations(Pageable pageable);
}