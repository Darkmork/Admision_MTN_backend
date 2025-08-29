package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.UsuarioAuthRequestDto;
import com.desafios.admision_mtn.dto.UsuarioDto;
import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    // Constructor eliminado - usando @RequiredArgsConstructor

    @GetMapping
    public List<UsuarioDto> getAllUsuarios() {
        return usuarioService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> getRankingUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            
            // Ordenar por puntaje descendente
            List<Map<String, Object>> ranking = usuarios.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getPuntaje(), u1.getPuntaje()))
                .map(usuario -> {
                    Map<String, Object> userRank = new HashMap<>();
                    userRank.put("id", usuario.getId());
                    userRank.put("username", usuario.getUsername());
                    userRank.put("puntaje", usuario.getPuntaje());
                    userRank.put("nivel", Math.max(1, usuario.getPuntaje() / 100));
                    return userRank;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            logger.error("[RANKING] Error obteniendo ranking: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> getUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            logger.debug("[USER_DETAIL] Usuario encontrado: {}, Puntaje: {}", usuario.getUsername(), usuario.getPuntaje());
            UsuarioDto dto = toDto(usuario);
            logger.debug("[USER_DETAIL] DTO creado, Puntaje en DTO: {}", dto.getPuntaje());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<UsuarioDto> createUsuario(@RequestBody UsuarioAuthRequestDto usuarioDto) {
        if (usuarioService.existsByUsername(usuarioDto.getUsername()) || usuarioService.existsByEmail(usuarioDto.getEmail())) {
            return ResponseEntity.status(409).build();
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDto.getUsername());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDto.getPassword())); // Encriptar la contraseña aquí
        usuario.setRol(usuarioDto.getRol());

        Usuario saved = usuarioService.save(usuario);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/puntaje")
    public ResponseEntity<Integer> getPuntajeUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        return usuarioOpt.map(usuario -> ResponseEntity.ok(usuario.getPuntaje()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/puntaje")
    public ResponseEntity<UsuarioDto> actualizarPuntaje(@PathVariable Long id, @RequestBody int nuevosPuntos) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        int puntajeAnterior = usuario.getPuntaje();
        usuario.setPuntaje(usuario.getPuntaje() + nuevosPuntos);
        Usuario updated = usuarioService.save(usuario);
        
        logger.info("[UPDATE_SCORE] Actualizando puntaje - Anterior: {}, Incremento: {}, Nuevo: {}", puntajeAnterior, nuevosPuntos, updated.getPuntaje());
        
        return ResponseEntity.ok(toDto(updated));
    }

    // Endpoint temporal para debug - establecer puntaje directamente
    @PutMapping("/{id}/puntaje/set")
    public ResponseEntity<UsuarioDto> establecerPuntaje(@PathVariable Long id, @RequestBody int puntaje) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        int puntajeAnterior = usuario.getPuntaje();
        usuario.setPuntaje(puntaje);
        Usuario updated = usuarioService.save(usuario);
        
        logger.info("[SET_SCORE] Estableciendo puntaje directo - Usuario ID: {}, Username: {}, Puntaje anterior: {}, Puntaje nuevo: {}, Puntaje guardado: {}", 
                   id, usuario.getUsername(), puntajeAnterior, puntaje, updated.getPuntaje());
        
        return ResponseEntity.ok(toDto(updated));
    }

    // Endpoint para inicializar puntos a todos los usuarios
    @PostMapping("/inicializar-puntos")
    public ResponseEntity<String> inicializarPuntos() {
        logger.info("[INIT_POINTS] Iniciando proceso de inicialización de puntos");
        
        List<Usuario> usuarios = usuarioService.findAll();
        logger.info("[INIT_POINTS] Total usuarios encontrados: {}", usuarios.size());
        
        int usuariosActualizados = 0;
        
        for (Usuario usuario : usuarios) {
            logger.debug("[INIT_POINTS] Usuario: {} (ID: {}) - Puntaje actual: {}", usuario.getUsername(), usuario.getId(), usuario.getPuntaje());
            
            if (usuario.getPuntaje() == 0) {
                logger.info("[INIT_POINTS] Asignando 100 puntos a: {}", usuario.getUsername());
                usuario.setPuntaje(100);
                
                try {
                    Usuario savedUser = usuarioService.save(usuario);
                    logger.info("[INIT_POINTS] Usuario guardado - Nuevo puntaje: {}", savedUser.getPuntaje());
                    usuariosActualizados++;
                } catch (Exception e) {
                    logger.error("[INIT_POINTS] Error guardando usuario {}: {}", usuario.getUsername(), e.getMessage(), e);
                }
            } else {
                logger.debug("[INIT_POINTS] Usuario {} ya tiene puntos: {}", usuario.getUsername(), usuario.getPuntaje());
            }
        }
        
        logger.info("[INIT_POINTS] Proceso completado - {} usuarios actualizados", usuariosActualizados);
        return ResponseEntity.ok("Puntos iniciales asignados a " + usuariosActualizados + " usuarios");
    }

    // Endpoint para debug completo de un usuario
    @GetMapping("/{id}/debug")
    public ResponseEntity<String> debugUsuario(@PathVariable Long id) {
        logger.debug("[USER_DEBUG] Iniciando debug completo para usuario ID: {}", id);
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        StringBuilder debug = new StringBuilder();
        debug.append("Usuario encontrado:\n");
        debug.append("ID: ").append(usuario.getId()).append("\n");
        debug.append("Username: ").append(usuario.getUsername()).append("\n");
        debug.append("Email: ").append(usuario.getEmail()).append("\n");
        debug.append("Rol: ").append(usuario.getRol()).append("\n");
        debug.append("Puntaje: ").append(usuario.getPuntaje()).append("\n");
        debug.append("Fecha Registro: ").append(usuario.getFechaRegistro()).append("\n");
        
        logger.debug("[USER_DEBUG] Información del usuario: {}", debug.toString());
        
        return ResponseEntity.ok(debug.toString());
    }

    // Utilidad interna para mapear entidad a DTO
    private UsuarioDto toDto(Usuario usuario) {
        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setPuntaje(usuario.getPuntaje());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        // No se incluye el password
        return dto;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioAuthRequestDto request) {
        var usuarioOpt = usuarioService.findByUsername(request.getUsername());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }
        var usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }
        // Opcional: genera un token o retorna los datos del usuario
        return ResponseEntity.ok().body(usuario);
    }
}
