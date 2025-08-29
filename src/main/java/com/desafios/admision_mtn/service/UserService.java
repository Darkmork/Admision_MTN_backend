package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.RegisterRequest;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.util.RutUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    // @Cacheable(value = "users", key = "#username")  // Temporalmente deshabilitado para debug
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("🔍 UserService: Loading user by email: {}", username);
        
        // Debug: verificar si existe con query nativa
        Long count = userRepository.countByEmailNative(username);
        log.info("🧪 DEBUG: Native count for email {}: {}", username, count);
        
        // Intentar con query nativa primero
        java.util.Optional<User> userOpt = userRepository.findByEmailNative(username);
        if (userOpt.isEmpty()) {
            log.error("❌ UserService: User not found with native query: {}", username);
            // Fallback al método original
            userOpt = userRepository.findByEmail(username);
        }
        
        User user = userOpt.orElseThrow(() -> {
                log.error("❌ UserService: User not found with both methods: {}", username);
                return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });
            
        log.info("✅ UserService: User found: {} - Active: {} - EmailVerified: {}", 
                 user.getEmail(), user.getActive(), user.getEmailVerified());
            
        if (!user.getActive()) {
            log.error("❌ UserService: User inactive: {}", username);
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        if (!user.getEmailVerified()) {
            log.error("❌ UserService: Email not verified: {}", username);
            throw new UsernameNotFoundException("Email no verificado: " + username);
        }

        log.info("🔐 UserService: Returning user details for: {}", username);
        return user;
    }
    
    @Transactional
    public User registerUser(RegisterRequest request) {
        // Verificar si el usuario ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con este email");
        }
        
        if (userRepository.existsByRut(request.getRut())) {
            throw new RuntimeException("Ya existe un usuario con este RUT");
        }
        
        // Crear nuevo usuario
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRut(RutUtil.formatRut(request.getRut())); // Formatear RUT automáticamente
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.APODERADO);
        user.setEmailVerified(true); // Se asume verificado porque pasó el proceso de verificación
        user.setActive(true);
        
        return userRepository.save(user);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public boolean existsByRut(String rut) {
        return userRepository.existsByRut(rut);
    }
    
    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public void markEmailAsVerified(String email) {
        userRepository.findByEmail(email)
            .ifPresent(user -> {
                user.setEmailVerified(true);
                userRepository.save(user);
            });
    }
    
    // @Cacheable(value = "users", key = "#email")  // Temporalmente deshabilitado para debug
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}