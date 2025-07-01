package com.desafios.robotcode.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "temas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Tema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Dificultad dificultad;
}