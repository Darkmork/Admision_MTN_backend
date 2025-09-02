package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.InterviewerSchedule;
import com.desafios.admision_mtn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface InterviewerScheduleRepository extends JpaRepository<InterviewerSchedule, Long> {

    /**
     * Buscar horarios activos de un entrevistador específico
     */
    List<InterviewerSchedule> findByInterviewerAndIsActiveTrue(User interviewer);

    /**
     * Buscar horarios de un entrevistador para un año específico
     */
    List<InterviewerSchedule> findByInterviewerAndYearAndIsActiveTrue(User interviewer, Integer year);

    /**
     * Buscar horarios recurrentes de un entrevistador para un día de la semana específico
     */
    @Query("SELECT s FROM InterviewerSchedule s WHERE s.interviewer = :interviewer " +
           "AND s.dayOfWeek = :dayOfWeek AND s.year = :year " +
           "AND s.scheduleType = 'RECURRING' AND s.isActive = true")
    List<InterviewerSchedule> findRecurringSchedules(@Param("interviewer") User interviewer, 
                                                   @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                                                   @Param("year") Integer year);

    /**
     * Buscar horarios para una fecha específica
     */
    @Query("SELECT s FROM InterviewerSchedule s WHERE s.interviewer = :interviewer " +
           "AND s.specificDate = :date AND s.scheduleType = 'SPECIFIC_DATE' AND s.isActive = true")
    List<InterviewerSchedule> findSpecificDateSchedules(@Param("interviewer") User interviewer, 
                                                      @Param("date") LocalDate date);

    /**
     * Buscar excepciones (días no disponibles) para una fecha específica
     */
    @Query("SELECT s FROM InterviewerSchedule s WHERE s.interviewer = :interviewer " +
           "AND s.specificDate = :date AND s.scheduleType = 'EXCEPTION' AND s.isActive = true")
    List<InterviewerSchedule> findExceptions(@Param("interviewer") User interviewer, 
                                           @Param("date") LocalDate date);

    /**
     * Buscar todos los entrevistadores disponibles en una fecha y hora específica
     */
    @Query(value = "SELECT DISTINCT u.* FROM users u " +
           "JOIN interviewer_schedules s ON u.id = s.interviewer_id " +
           "WHERE s.is_active = true AND s.year = :year AND " +
           "((s.schedule_type = 'RECURRING' AND EXTRACT(DOW FROM CAST(:date AS DATE)) = s.day_of_week) OR " +
           " (s.schedule_type = 'SPECIFIC_DATE' AND s.specific_date = :date)) AND " +
           "s.start_time <= :time AND s.end_time >= :time AND " +
           "u.id NOT IN (" +
           "    SELECT e.interviewer_id FROM interviewer_schedules e WHERE " +
           "    e.schedule_type = 'EXCEPTION' AND e.specific_date = :date AND e.is_active = true" +
           ")", nativeQuery = true)
    List<User> findAvailableInterviewers(@Param("date") LocalDate date, 
                                       @Param("time") LocalTime time, 
                                       @Param("year") Integer year);

    /**
     * Buscar horarios que se superponen para detectar conflictos
     */
    @Query("SELECT s FROM InterviewerSchedule s WHERE s.interviewer = :interviewer " +
           "AND s.isActive = true AND s.id != :excludeId " +
           "AND ((s.dayOfWeek = :dayOfWeek AND s.year = :year AND s.scheduleType = 'RECURRING') OR " +
           "     (s.specificDate = :specificDate AND s.scheduleType = 'SPECIFIC_DATE')) " +
           "AND ((s.startTime <= :endTime AND s.endTime >= :startTime))")
    List<InterviewerSchedule> findConflictingSchedules(@Param("interviewer") User interviewer,
                                                     @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                     @Param("specificDate") LocalDate specificDate,
                                                     @Param("year") Integer year,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime,
                                                     @Param("excludeId") Long excludeId);

    /**
     * Contar horarios activos por entrevistador y año
     */
    @Query("SELECT COUNT(s) FROM InterviewerSchedule s WHERE s.interviewer = :interviewer " +
           "AND s.year = :year AND s.isActive = true")
    Long countActiveSchedulesByInterviewerAndYear(@Param("interviewer") User interviewer, 
                                                @Param("year") Integer year);

    /**
     * Buscar todos los horarios de un año específico
     */
    List<InterviewerSchedule> findByYearAndIsActiveTrue(Integer year);

    /**
     * Buscar entrevistadores por rol que tienen horarios configurados
     */
    @Query("SELECT DISTINCT s.interviewer FROM InterviewerSchedule s WHERE " +
           "s.year = :year AND s.isActive = true AND " +
           "s.interviewer.role IN ('CYCLE_DIRECTOR', 'PSYCHOLOGIST', 'COORDINATOR')")
    List<User> findInterviewersWithSchedules(@Param("year") Integer year);

    /**
     * Estadísticas de carga de trabajo por entrevistador
     */
    @Query("SELECT s.interviewer.id, s.interviewer.firstName, s.interviewer.lastName, " +
           "COUNT(s) as scheduleCount " +
           "FROM InterviewerSchedule s WHERE s.year = :year AND s.isActive = true " +
           "GROUP BY s.interviewer.id, s.interviewer.firstName, s.interviewer.lastName")
    List<Object[]> getWorkloadStatistics(@Param("year") Integer year);
}