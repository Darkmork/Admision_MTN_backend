package com.desafios.robotcode.service;

import com.desafios.robotcode.model.Usuario;
import com.desafios.robotcode.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
        System.out.println("=== DEBUG USUARIO SERVICE SAVE ===");
        System.out.println("Guardando usuario: " + usuario.getUsername() + " (ID: " + usuario.getId() + ")");
        System.out.println("Puntaje antes de guardar: " + usuario.getPuntaje());
        
        Usuario saved = usuarioRepository.save(usuario);
        
        System.out.println("Usuario guardado con ID: " + saved.getId());
        System.out.println("Puntaje después de guardar: " + saved.getPuntaje());
        System.out.println("================================");
        
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
