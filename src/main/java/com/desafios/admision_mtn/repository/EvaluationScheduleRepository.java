package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.EvaluationSchedule;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationScheduleRepository extends JpaRepository<EvaluationSchedule, Long> {

    // Encontrar programaciones por aplicación específica
    List<EvaluationSchedule> findByApplicationId(Long applicationId);

    // Encontrar programaciones genéricas por tipo y nivel
    List<EvaluationSchedule> findByEvaluationTypeAndGradeLevelAndApplicationIsNull(
        Evaluation.EvaluationType evaluationType, 
        String gradeLevel
    );

    // Programaciones por evaluador y rango de fechas
    @Query("SELECT es FROM EvaluationSchedule es WHERE es.evaluator.id = :evaluatorId " +
           "AND es.scheduledDate BETWEEN :startDate AND :endDate " +
           "ORDER BY es.scheduledDate ASC")
    List<EvaluationSchedule> findByEvaluatorAndDateRange(
        @Param("evaluatorId") Long evaluatorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Programaciones por estado
    List<EvaluationSchedule> findByStatus(EvaluationSchedule.ScheduleStatus status);

    // Programaciones que requieren confirmación antes de una fecha
    @Query("SELECT es FROM EvaluationSchedule es WHERE es.requiresConfirmation = true " +
           "AND es.confirmationDeadline < :deadline AND es.confirmedAt IS NULL " +
           "AND es.status = 'SCHEDULED'")
    List<EvaluationSchedule> findPendingConfirmations(@Param("deadline") LocalDateTime deadline);

    // Próximas citas de una familia
    @Query("SELECT es FROM EvaluationSchedule es WHERE " +
           "(es.application.id = :applicationId OR " +
           "(es.application IS NULL AND es.gradeLevel = :gradeLevel AND es.evaluationType IN :evaluationTypes)) " +
           "AND es.scheduledDate > :currentDate " +
           "AND es.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY es.scheduledDate ASC")
    List<EvaluationSchedule> findUpcomingForFamily(
        @Param("applicationId") Long applicationId,
        @Param("gradeLevel") String gradeLevel,
        @Param("evaluationTypes") List<Evaluation.EvaluationType> evaluationTypes,
        @Param("currentDate") LocalDateTime currentDate
    );

    // Horarios disponibles para un evaluador en un día
    @Query("SELECT es FROM EvaluationSchedule es WHERE es.evaluator.id = :evaluatorId " +
           "AND DATE(es.scheduledDate) = DATE(:date) " +
           "AND es.status NOT IN ('CANCELLED') " +
           "ORDER BY es.scheduledDate ASC")
    List<EvaluationSchedule> findEvaluatorScheduleByDate(
        @Param("evaluatorId") Long evaluatorId, 
        @Param("date") LocalDateTime date
    );

    // Conflictos de horario para un evaluador - simplificado
    @Query("SELECT es FROM EvaluationSchedule es WHERE es.evaluator.id = :evaluatorId " +
           "AND es.scheduledDate BETWEEN :startTime AND :endTime " +
           "AND es.status NOT IN ('CANCELLED') AND es.id != :excludeId")
    List<EvaluationSchedule> findScheduleConflicts(
        @Param("evaluatorId") Long evaluatorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );

    // Estadísticas de programación
    @Query("SELECT es.status, COUNT(es) FROM EvaluationSchedule es " +
           "WHERE es.scheduledDate BETWEEN :startDate AND :endDate " +
           "GROUP BY es.status")
    List<Object[]> getScheduleStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}