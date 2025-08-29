package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    
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
        logger.debug("[USER_SERVICE] Guardando usuario: {} (ID: {}) - Puntaje antes: {}", 
                    usuario.getUsername(), usuario.getId(), usuario.getPuntaje());
        
        Usuario saved = usuarioRepository.save(usuario);
        
        logger.debug("[USER_SERVICE] Usuario guardado - ID: {}, Puntaje después: {}", 
                    saved.getId(), saved.getPuntaje());
        
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
