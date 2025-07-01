package com.desafios.robotcode.model;

import com.desafios.robotcode.model.RolUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    @Column(nullable = false, name = "puntaje")
    private int puntaje = 0; // Valor por defecto, 0

    @Column(nullable = false, name = "email_verified")
    private boolean emailVerified = false;

    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        System.out.println("PrePersist - Usuario: " + username + ", Puntaje: " + puntaje);
    }

    @PreUpdate
    public void preUpdate() {
        System.out.println("PreUpdate - Usuario: " + username + ", Puntaje: " + puntaje);
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        System.out.println("setPuntaje called - Usuario: " + this.username + ", Nuevo puntaje: " + puntaje);
        this.puntaje = puntaje;
    }
}
