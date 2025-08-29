package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔍 Loading user by email: {}", email);
        
        // En este sistema, el "username" es realmente el email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ User not found: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        log.info("✅ User found: {} - Active: {} - EmailVerified: {}", 
                 user.getEmail(), user.getActive(), user.getEmailVerified());

        if (!user.getActive()) {
            log.error("❌ User inactive: {}", email);
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        if (!user.getEmailVerified()) {
            log.error("❌ Email not verified: {}", email);
            throw new UsernameNotFoundException("Email no verificado: " + email);
        }

        log.info("🔐 Returning user details for: {}", email);
        // La entidad User ya implementa UserDetails, así que podemos retornarla directamente
        return user;
    }

    // Método auxiliar para obtener el usuario completo por email
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}