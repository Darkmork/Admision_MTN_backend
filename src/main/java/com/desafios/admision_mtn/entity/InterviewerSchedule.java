package com.desafios.admision_mtn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "interviewer_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewerSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    @Builder.Default
    private ScheduleType scheduleType = ScheduleType.RECURRING;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum para tipo de horario
    public enum ScheduleType {
        RECURRING,    // Horario recurrente semanal
        SPECIFIC_DATE, // Fecha específica
        EXCEPTION     // Excepción (día no disponible)
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Método de utilidad para verificar si el horario aplica en una fecha específica
    public boolean appliesOnDate(LocalDate date) {
        if (!isActive) {
            return false;
        }

        switch (scheduleType) {
            case SPECIFIC_DATE:
                return specificDate != null && specificDate.equals(date);
            case RECURRING:
                return date.getYear() == year && date.getDayOfWeek() == dayOfWeek;
            case EXCEPTION:
                return false; // Las excepciones no aplican, solo bloquean
            default:
                return false;
        }
    }

    // Método para verificar si un horario está disponible en una fecha y hora específica
    public boolean isAvailableAt(LocalDate date, LocalTime time) {
        if (!appliesOnDate(date)) {
            return false;
        }

        // Corregir la lógica: el tiempo debe estar entre startTime (inclusive) y endTime (exclusive)
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    // Método para obtener la duración del horario en minutos
    public long getDurationMinutes() {
        return startTime.until(endTime, java.time.temporal.ChronoUnit.MINUTES);
    }
}