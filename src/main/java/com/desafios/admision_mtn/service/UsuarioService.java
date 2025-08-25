package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public Usuario save(Usuario usuario) {
        log.debug("=== DEBUG USUARIO SERVICE SAVE ===");
        log.debug("Guardando usuario: {} (ID: {})", usuario.getUsername(), usuario.getId());
        log.debug("Puntaje antes de guardar: {}", usuario.getPuntaje());

        Usuario saved = usuarioRepository.save(usuario);

        log.debug("Usuario guardado con ID: {}", saved.getId());
        log.debug("Puntaje después de guardar: {}", saved.getPuntaje());
        log.debug("================================");

        return saved;
    }

    @Override
    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }


}
