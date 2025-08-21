package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.UsuarioAuthRequestDto;
import com.desafios.admision_mtn.dto.UsuarioDto;
import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.model.RolUsuario;
import com.desafios.admision_mtn.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuario-auth")
public class UsuarioAuthController {

    private final UsuarioService usuarioService;

    public UsuarioAuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDto> register(@RequestBody UsuarioAuthRequestDto dto) {
        if (usuarioService.existsByUsername(dto.getUsername()) || usuarioService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(409).build();
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword()); // En producción, encripta la contraseña
        usuario.setRol(RolUsuario.USER);

        Usuario saved = usuarioService.save(usuario);

        UsuarioDto resp = new UsuarioDto();
        resp.setId(saved.getId());
        resp.setUsername(saved.getUsername());
        resp.setEmail(saved.getEmail());
        resp.setRol(saved.getRol());
        resp.setFechaRegistro(saved.getFechaRegistro());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioDto> login(@RequestBody UsuarioAuthRequestDto dto) {
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(dto.getUsername());
        if (usuarioOpt.isPresent() && usuarioOpt.get().getPassword().equals(dto.getPassword())) {
            Usuario usuario = usuarioOpt.get();
            UsuarioDto resp = new UsuarioDto();
            resp.setId(usuario.getId());
            resp.setUsername(usuario.getUsername());
            resp.setEmail(usuario.getEmail());
            resp.setRol(usuario.getRol());
            resp.setFechaRegistro(usuario.getFechaRegistro());
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}
