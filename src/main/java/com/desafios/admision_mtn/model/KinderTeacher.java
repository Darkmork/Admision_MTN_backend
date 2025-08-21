package com.desafios.admision_mtn.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "kinder_teachers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class KinderTeacher {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private KinderLevel assignedLevel; // Solo prekinder o kinder

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "kinder_teacher_specializations", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "specialization")
    private List<String> specializations; // Ej: "Desarrollo Motor", "Lenguaje Inicial"

    private Integer yearsOfExperience;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "kinder_teacher_qualifications", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "qualification")
    private List<String> qualifications;
}