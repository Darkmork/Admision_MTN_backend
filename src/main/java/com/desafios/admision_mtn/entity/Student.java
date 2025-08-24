package com.desafios.admision_mtn.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName; // Nombres

    @Column(name = "paternal_last_name", nullable = false)
    private String lastName; // Apellido Paterno

    @Column(name = "maternal_last_name", nullable = false)
    private String maternalLastName; // Apellido Materno

    @Column(name = "rut", nullable = false, unique = true)
    private String rut;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email")
    private String email; // Opcional para estudiantes

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "grade_applied", nullable = false)
    private String gradeApplied;

    @Column(name = "school_applied", nullable = false)
    private String schoolApplied; // "MONTE_TABOR" para niños, "NAZARET" para niñas

    @Column(name = "current_school")
    private String currentSchool; // Solo para estudiantes con escolaridad previa

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @OneToOne(mappedBy = "student")
    @JsonIgnore
    private Application application;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Método de conveniencia para compatibilidad con el código existente
    public String getPaternalLastName() {
        return lastName; // lastName ya es el apellido paterno
    }
}