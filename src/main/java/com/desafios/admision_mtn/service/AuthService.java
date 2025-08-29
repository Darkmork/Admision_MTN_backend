package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.AuthRequest;
import com.desafios.admision_mtn.dto.AuthResponse;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.UserRepository;
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

    private final UserRepository userRepository;
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

        // Buscar el usuario en la base de datos (request.getUsername() es realmente el email)
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Generar tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Calcular tiempo de expiración en segundos
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

        return AuthResponse.builder()
                .token(accessToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
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
            User user = userDetailsService.findUserByEmail(username);

            // Validar el refresh token
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new RuntimeException("Refresh token inválido o expirado");
            }

            // Generar nuevos tokens
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            // Calcular tiempo de expiración
            long expiresIn = jwtService.getExpirationTime(newAccessToken) / 1000;

            return AuthResponse.builder()
                    .token(newAccessToken)
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())
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
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Verifica si un usuario tiene un rol específico
     */
    public boolean hasRole(String email, String role) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            return user != null && user.getRole().name().equals(role);
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