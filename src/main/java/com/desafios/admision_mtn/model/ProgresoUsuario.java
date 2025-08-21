package com.desafios.admision_mtn.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProgresoUsuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    private Problema problema;

    @Enumerated(EnumType.STRING)
    private EstadoProgreso estado;

    private int intentos;

    private LocalDateTime ultimaModificacion;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        ultimaModificacion = LocalDateTime.now();
    }
}
