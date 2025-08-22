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
    
    List<Interview> findByMode(InterviewMode mode);
    
    List<Interview> findByInterviewerId(Long interviewerId);
    
    List<Interview> findByApplicationId(Long applicationId);
    
    // Búsquedas por fecha
    List<Interview> findByScheduledDate(LocalDate date);
    
    List<Interview> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Entrevistas del día actual
    @Query("SELECT i FROM Interview i WHERE i.scheduledDate = CURRENT_DATE")
    List<Interview> findTodaysInterviews();
    
    // Entrevistas próximas (próximas 24 horas)
    @Query(value = "SELECT * FROM interviews i WHERE " +
           "i.scheduled_date >= CURRENT_DATE " +
           "AND i.scheduled_date <= CURRENT_DATE + INTERVAL '1 day'", 
           nativeQuery = true)
    List<Interview> findUpcomingInterviews();
    
    // Entrevistas vencidas
    @Query("SELECT i FROM Interview i WHERE " +
           "(i.scheduledDate < CURRENT_DATE OR " +
           "(i.scheduledDate = CURRENT_DATE AND i.scheduledTime < CURRENT_TIME)) " +
           "AND i.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Interview> findOverdueInterviews();
    
    // Entrevistas por entrevistador y fecha
    @Query("SELECT i FROM Interview i WHERE i.interviewer.id = :interviewerId " +
           "AND i.scheduledDate BETWEEN :startDate AND :endDate")
    List<Interview> findByInterviewerAndDateRange(
        @Param("interviewerId") Long interviewerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Búsqueda con filtros múltiples
    @Query("SELECT i FROM Interview i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:type IS NULL OR i.type = :type) AND " +
           "(:mode IS NULL OR i.mode = :mode) AND " +
           "(:interviewerId IS NULL OR i.interviewer.id = :interviewerId) AND " +
           "(:startDate IS NULL OR i.scheduledDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.scheduledDate <= :endDate)")
    Page<Interview> findWithFilters(
        @Param("status") InterviewStatus status,
        @Param("type") InterviewType type,
        @Param("mode") InterviewMode mode,
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
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.mode = :mode")
    long countByMode(@Param("mode") InterviewMode mode);
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.result = :result")
    long countByResult(@Param("result") InterviewResult result);
    
    // Promedio de puntuaciones
    @Query("SELECT AVG(i.score) FROM Interview i WHERE i.score IS NOT NULL")
    Optional<Double> findAverageScore();
    
    @Query("SELECT AVG(i.score) FROM Interview i WHERE i.score IS NOT NULL AND i.type = :type")
    Optional<Double> findAverageScoreByType(@Param("type") InterviewType type);
    
    // Estadísticas mensuales
    @Query(value = "SELECT TO_CHAR(scheduled_date, 'YYYY-MM') as month, COUNT(*) " +
           "FROM interviews " +
           "GROUP BY TO_CHAR(scheduled_date, 'YYYY-MM') " +
           "ORDER BY month", 
           nativeQuery = true)
    List<Object[]> findMonthlyStatistics();
    
    // Entrevistas que requieren seguimiento
    @Query("SELECT i FROM Interview i WHERE i.followUpRequired = true AND i.status = 'COMPLETED'")
    List<Interview> findRequiringFollowUp();
    
    // Verificar disponibilidad de horario para un entrevistador
    @Query("SELECT COUNT(i) FROM Interview i WHERE " +
           "i.interviewer.id = :interviewerId AND " +
           "i.scheduledDate = :date AND " +
           "i.scheduledTime = :time AND " +
           "i.status NOT IN ('CANCELLED', 'COMPLETED')")
    long countConflictingInterviews(
        @Param("interviewerId") Long interviewerId,
        @Param("date") LocalDate date,
        @Param("time") java.time.LocalTime time
    );
    
    // Entrevistas por rango de fechas y estado
    @Query("SELECT i FROM Interview i WHERE " +
           "i.scheduledDate BETWEEN :startDate AND :endDate AND " +
           "i.status IN :statuses " +
           "ORDER BY i.scheduledDate, i.scheduledTime")
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
    
    // Distribución de entrevistas por modalidad
    @Query("SELECT i.mode, COUNT(i) FROM Interview i GROUP BY i.mode")
    List<Object[]> findModeDistribution();
    
    // Entrevistas completadas con resultado positivo
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.status = 'COMPLETED' AND i.result = 'POSITIVE'")
    long countPositiveResults();
    
    // Buscar entrevistas por múltiples criterios con paginación
    @Query("SELECT i FROM Interview i WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(CONCAT(i.application.student.firstName, ' ', i.application.student.lastName, ' ', i.application.student.maternalLastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(i.interviewer.firstName, ' ', i.interviewer.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY i.scheduledDate DESC, i.scheduledTime DESC")
    Page<Interview> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}