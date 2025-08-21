package com.desafios.admision_mtn.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "psychologists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Psychologist {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private PsychologySpecialty specialty;

    private String licenseNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "psychologist_grades", joinColumns = @JoinColumn(name = "psychologist_id"))
    @Column(name = "grade")
    private List<String> assignedGrades; // Niveles que puede evaluar

    @Column(nullable = false)
    private boolean canConductInterviews = false;

    @Column(nullable = false)
    private boolean canPerformPsychologicalEvaluations = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "psychologist_specialized_areas", joinColumns = @JoinColumn(name = "psychologist_id"))
    @Column(name = "area")
    private List<String> specializedAreas; // Ej: "Dificultades de Aprendizaje", "Trastornos del Desarrollo"
}