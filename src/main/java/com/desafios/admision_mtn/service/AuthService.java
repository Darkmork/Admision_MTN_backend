package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.AuthRequest;
import com.desafios.admision_mtn.dto.AuthResponse;
import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Autentica un usuario y genera tokens JWT
     */
    public AuthResponse authenticate(AuthRequest request) {
        // Autenticar usando Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.isActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Generar tokens
        String accessToken = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        // Calcular tiempo de expiración en segundos
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

        return AuthResponse.builder()
                .token(accessToken)
                .email(usuario.getEmail())
                .firstName(usuario.getFirstName())
                .lastName(usuario.getLastName())
                .role(usuario.getRol().name())
                .success(true)
                .message("Autenticación exitosa")
                .build();
    }

    /**
     * Refresca el access token usando un refresh token válido
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // Verificar que es un refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new RuntimeException("Token inválido: no es un refresh token");
            }

            // Extraer username del refresh token
            String username = jwtService.extractUsername(refreshToken);
            
            // Cargar usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Usuario usuario = userDetailsService.findUsuarioByUsername(username);

            // Validar el refresh token
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new RuntimeException("Refresh token inválido o expirado");
            }

            // Generar nuevos tokens
            String newAccessToken = jwtService.generateToken(usuario);
            String newRefreshToken = jwtService.generateRefreshToken(usuario);

            // Calcular tiempo de expiración
            long expiresIn = jwtService.getExpirationTime(newAccessToken) / 1000;

            return AuthResponse.builder()
                    .token(newAccessToken)
                    .email(usuario.getEmail())
                    .firstName(usuario.getFirstName())
                    .lastName(usuario.getLastName())
                    .role(usuario.getRol().name())
                    .success(true)
                    .message("Token refrescado exitosamente")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error al refrescar token: " + e.getMessage());
        }
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad
     */
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        String username = authentication.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Verifica si un usuario tiene un rol específico
     */
    public boolean hasRole(String username, String role) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElse(null);
            
            return usuario != null && usuario.getRol().name().equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario actual tiene un permiso específico
     */
    public boolean hasPermission(String permission) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return false;
            }

            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission));
        } catch (Exception e) {
            return false;
        }
    }
}