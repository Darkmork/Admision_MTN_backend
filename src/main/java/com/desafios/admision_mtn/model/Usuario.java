package com.desafios.admision_mtn.model;

import com.desafios.admision_mtn.model.RolUsuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Slf4j
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

    // Campos adicionales para personal del colegio
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
    
    @Column(nullable = false)
    private boolean isActive = true;

    // Campo para sistema original de competencias
    @Column(nullable = false, name = "puntaje")
    private int puntaje = 0; // Valor por defecto, 0

    @Column(nullable = false, name = "email_verified")
    private boolean emailVerified = false;

    private LocalDateTime fechaRegistro;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        log.debug("PrePersist - Usuario: {}, Puntaje: {}", username, puntaje);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        log.debug("PreUpdate - Usuario: {}, Puntaje: {}", username, puntaje);
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        log.debug("setPuntaje called - Usuario: {}, Nuevo puntaje: {}", this.username, puntaje);
        this.puntaje = puntaje;
    }

    // Métodos de conveniencia para personal del colegio
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }

    public boolean isSchoolStaff() {
        return rol == RolUsuario.PROFESSOR || 
               rol == RolUsuario.KINDER_TEACHER || 
               rol == RolUsuario.PSYCHOLOGIST || 
               rol == RolUsuario.SUPPORT_STAFF;
    }
}
