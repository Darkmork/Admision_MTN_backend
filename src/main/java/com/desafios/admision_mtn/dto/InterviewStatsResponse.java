package com.desafios.admision_mtn.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStatsResponse {

    // Métricas principales
    private long totalInterviews;
    private long scheduledInterviews;
    private long completedInterviews;
    private long cancelledInterviews;
    private long noShowInterviews;
    private long pendingInterviews;
    
    // Métricas de resultados
    private long positiveResults;
    private long neutralResults;
    private long negativeResults;
    private long pendingReviewResults;
    private long requiresFollowUpResults;
    
    // Promedios y tasas
    private double averageScore;
    private double completionRate;
    private double cancellationRate;
    private double successRate;
    
    // Distribuciones
    private Map<String, Long> statusDistribution;
    private Map<String, Long> typeDistribution;
    private Map<String, Long> modeDistribution;
    private Map<String, Long> resultDistribution;
    
    // Tendencias mensuales
    private Map<String, Long> monthlyTrends;
    
    // Métricas adicionales
    private long followUpRequired;
    private long upcomingInterviews;
    private long overdueInterviews;
}