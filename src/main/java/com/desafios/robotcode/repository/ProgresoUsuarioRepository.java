package com.desafios.robotcode.repository;

import com.desafios.robotcode.model.ProgresoUsuario;
import com.desafios.robotcode.model.Usuario;
import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.model.EstadoProgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoUsuarioRepository extends JpaRepository<ProgresoUsuario, Long> {
    List<ProgresoUsuario> findByUsuario(Usuario usuario);
    Optional<ProgresoUsuario> findByUsuarioAndProblema(Usuario usuario, Problema problema);

    // Nuevo método para saber si un usuario ya resolvió un problema
    boolean existsByUsuarioAndProblemaAndEstado(Usuario usuario, Problema problema, EstadoProgreso estado);
}
