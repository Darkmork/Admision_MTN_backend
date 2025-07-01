
// Ranking.java
package com.desafios.robotcode.model;

import com.desafios.robotcode.model.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ranking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Usuario usuario;

    private int puntaje;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
