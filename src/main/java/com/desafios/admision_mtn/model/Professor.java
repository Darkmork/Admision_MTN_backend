package com.desafios.admision_mtn.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "professors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Professor {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "professor_subjects", joinColumns = @JoinColumn(name = "professor_id"))
    @Column(name = "subject")
    private List<Subject> subjects; // Solo Matemática, Lenguaje, Inglés

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "professor_grades", joinColumns = @JoinColumn(name = "professor_id"))
    @Column(name = "grade")
    private List<String> assignedGrades;

    private String department;
    private Integer yearsOfExperience;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "professor_qualifications", joinColumns = @JoinColumn(name = "professor_id"))
    @Column(name = "qualification")
    private List<String> qualifications;

    @Column(nullable = false)
    private boolean isAdmin = false;

    public enum Subject {
        MATH("Matemática"),
        SPANISH("Lenguaje"),
        ENGLISH("Inglés");

        private final String displayName;

        Subject(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}