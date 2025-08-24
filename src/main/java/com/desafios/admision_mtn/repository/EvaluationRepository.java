package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    List<Evaluation> findByApplicationOrderByCreatedAtDesc(Application application);
    
    List<Evaluation> findByApplication_IdOrderByCreatedAtDesc(Long applicationId);
    
    List<Evaluation> findByEvaluatorOrderByCreatedAtDesc(User evaluator);
    
    List<Evaluation> findByEvaluator_IdOrderByCreatedAtDesc(Long evaluatorId);
    
    @Query("SELECT e FROM Evaluation e WHERE e.application.id = :applicationId AND e.evaluationType = :evaluationType")
    Optional<Evaluation> findByApplicationIdAndEvaluationType(@Param("applicationId") Long applicationId, 
                                                              @Param("evaluationType") Evaluation.EvaluationType evaluationType);
    
    @Query("SELECT e FROM Evaluation e WHERE e.evaluator.id = :evaluatorId AND e.status = :status ORDER BY e.createdAt DESC")
    List<Evaluation> findByEvaluatorIdAndStatus(@Param("evaluatorId") Long evaluatorId, 
                                               @Param("status") Evaluation.EvaluationStatus status);
    
    @Query("SELECT e FROM Evaluation e WHERE e.application.id = :applicationId AND e.status = :status")
    List<Evaluation> findByApplicationIdAndStatus(@Param("applicationId") Long applicationId, 
                                                 @Param("status") Evaluation.EvaluationStatus status);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.application.id = :applicationId AND e.status = 'COMPLETED'")
    Long countCompletedEvaluationsByApplicationId(@Param("applicationId") Long applicationId);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.application.id = :applicationId")
    Long countTotalEvaluationsByApplicationId(@Param("applicationId") Long applicationId);
    
    @Query("SELECT e FROM Evaluation e WHERE e.evaluator.email = :evaluatorEmail ORDER BY e.createdAt DESC")
    List<Evaluation> findByEvaluatorEmailOrderByCreatedAtDesc(@Param("evaluatorEmail") String evaluatorEmail);
    
    // Buscar evaluaciones pendientes para un evaluador específico
    @Query("SELECT e FROM Evaluation e WHERE e.evaluator.id = :evaluatorId AND e.status IN ('PENDING', 'IN_PROGRESS') ORDER BY e.createdAt ASC")
    List<Evaluation> findPendingEvaluationsByEvaluatorId(@Param("evaluatorId") Long evaluatorId);
    
    // Buscar todas las evaluaciones de una aplicación con sus evaluadores cargados
    @Query("SELECT e FROM Evaluation e JOIN FETCH e.evaluator WHERE e.application.id = :applicationId ORDER BY e.evaluationType, e.createdAt DESC")
    List<Evaluation> findByApplicationIdWithEvaluator(@Param("applicationId") Long applicationId);
    
    // Contar evaluaciones por evaluador
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.evaluator.id = :evaluatorId")
    Long countByEvaluatorId(@Param("evaluatorId") Long evaluatorId);
    
    // Verificar si un evaluador tiene evaluaciones
    boolean existsByEvaluatorId(Long evaluatorId);
    
    // Métodos adicionales para el dashboard
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.evaluationType = :type")
    long countByType(@Param("type") Evaluation.EvaluationType type);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.status = :status")
    long countByStatus(@Param("status") Evaluation.EvaluationStatus status);
    
    @Query("SELECT e FROM Evaluation e WHERE e.completionDate >= :fromDate AND e.status = com.desafios.admision_mtn.entity.Evaluation.EvaluationStatus.COMPLETED ORDER BY e.completionDate DESC")
    List<Evaluation> findCompletedFromDate(@Param("fromDate") java.time.LocalDateTime fromDate);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.status = :status AND e.createdAt <= :cutoffDate")
    long countOverdueEvaluations(@Param("status") Evaluation.EvaluationStatus status, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}