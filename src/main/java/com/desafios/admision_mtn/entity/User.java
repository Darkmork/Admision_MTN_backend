package com.desafios.admision_mtn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String rut;
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.APODERADO;
    
    @Enumerated(EnumType.STRING)
    private EducationalLevel educationalLevel;
    
    @Enumerated(EnumType.STRING)
    private Subject subject;
    
    @Column(nullable = false)
    private Boolean emailVerified = false;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return active;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return active;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }
    
    @Override
    public boolean isEnabled() {
        return active && emailVerified;
    }
    
    public enum UserRole {
        APODERADO("Apoderado"),
        ADMIN("Administrador"),
        TEACHER("Profesor"),
        COORDINATOR("Coordinador"),
        PSYCHOLOGIST("Psicólogo/a"),
        CYCLE_DIRECTOR("Director de Ciclo");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum EducationalLevel {
        PRESCHOOL("Prebásica (Kinder - 2° Básico)"),
        BASIC("Básica (3° - 8° Básico)"),
        HIGH_SCHOOL("Media (I° - IV° Medio)"),
        ALL_LEVELS("Todos los Niveles");

        private final String displayName;

        EducationalLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Subject {
        GENERAL("Educación General (Prebásica)"),
        LANGUAGE("Lenguaje y Literatura"),
        MATHEMATICS("Matemáticas"),
        ENGLISH("Inglés"),
        ALL_SUBJECTS("Todas las Asignaturas");

        private final String displayName;

        Subject(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}