package com.desafios.robotcode.service;

import com.desafios.robotcode.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    List<Usuario> getAll();
    Optional<Usuario> findById(Long id);
    Usuario save(Usuario usuario);
    void delete(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
}
