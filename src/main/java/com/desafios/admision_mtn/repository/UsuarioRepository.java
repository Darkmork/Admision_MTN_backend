// UsuarioRepository.java
package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.model.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Métodos para personal del colegio
    List<Usuario> findByRolIn(List<RolUsuario> roles);
    List<Usuario> findByRolInAndIsActiveTrue(List<RolUsuario> roles);
    List<Usuario> findByRolAndIsActiveTrue(RolUsuario rol);
    List<Usuario> findByIsActiveTrue();
}
