package com.desafios.admision_mtn.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "problemas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Problema {
    @Id
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String codigoInicial;

    @Column(columnDefinition = "TEXT")
    private String solucionCorrecta;

    @Column(columnDefinition = "TEXT")
    private String testCasesJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    private Tema tema;

    // **Agrega esto:**
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Dificultad dificultad;
}



