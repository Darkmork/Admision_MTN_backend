package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.model.Dificultad;
import com.desafios.admision_mtn.model.EstadoProgreso;
import com.desafios.admision_mtn.model.ProgresoUsuario;
import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.model.Problema;
import com.desafios.admision_mtn.repository.ProgresoUsuarioRepository;
import com.desafios.admision_mtn.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgresoUsuarioService {

    private final ProgresoUsuarioRepository progresoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public ProgresoUsuarioService(
            ProgresoUsuarioRepository progresoUsuarioRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.progresoUsuarioRepository = progresoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ProgresoUsuario> findByUsuario(Usuario usuario) {
        return progresoUsuarioRepository.findByUsuario(usuario);
    }

    public Optional<ProgresoUsuario> findByUsuarioAndProblema(Usuario usuario, Problema problema) {
        return progresoUsuarioRepository.findByUsuarioAndProblema(usuario, problema);
    }

    /**
     * Guarda el progreso sin manejar puntos automáticamente.
     * Los puntos deben ser manejados explícitamente por el controlador.
     */
    public ProgresoUsuario save(ProgresoUsuario progresoUsuario) {
        return progresoUsuarioRepository.save(progresoUsuario);
    }

    public int obtenerPuntajePorDificultad(Dificultad dificultad) {
        switch (dificultad) {
            case HARD: return 100;
            case INTERMEDIATE: return 70;
            case EASY: return 50;
            default: return 0;
        }
    }

    // Método sobrecargado para manejar dificultades como string (desde frontend)
    public int obtenerPuntajePorDificultad(String dificultadStr) {
        if (dificultadStr == null) return 0;
        
        switch (dificultadStr.toLowerCase()) {
            case "difícil":
            case "difficult":
            case "hard":
                return 100;
            case "intermedio":
            case "intermediate":
            case "medium":
                return 70;
            case "fácil":
            case "facil":
            case "easy":
                return 50;
            default:
                return 0;
        }
    }

    // Si necesitas actualizar el puntaje manualmente
    public void actualizarPuntajeUsuario(Usuario usuario, int puntosGanados) {
        usuario.setPuntaje(usuario.getPuntaje() + puntosGanados);
        usuarioRepository.save(usuario);
    }
}
