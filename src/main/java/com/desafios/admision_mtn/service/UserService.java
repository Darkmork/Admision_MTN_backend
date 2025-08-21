package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.RegisterRequest;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.util.RutUtil;
import lombok.RequiredArgsConstructor;
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
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
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
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public boolean existsByRut(String rut) {
        return userRepository.existsByRut(rut);
    }
    
    @Transactional
    public void markEmailAsVerified(String email) {
        userRepository.findByEmail(email)
            .ifPresent(user -> {
                user.setEmailVerified(true);
                userRepository.save(user);
            });
    }
}