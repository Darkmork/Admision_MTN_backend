package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActive()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Agregar rol principal
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        
        // Agregar permisos específicos basados en el rol
        switch (usuario.getRol()) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_USERS"));
                authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_APPLICATIONS"));
                authorities.add(new SimpleGrantedAuthority("PERM_VIEW_REPORTS"));
                authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_SETTINGS"));
                break;
                
            case PROFESSOR:
                authorities.add(new SimpleGrantedAuthority("PERM_EVALUATE_EXAMS"));
                authorities.add(new SimpleGrantedAuthority("PERM_VIEW_STUDENTS"));
                authorities.add(new SimpleGrantedAuthority("PERM_CREATE_MATERIALS"));
                break;
                
            case KINDER_TEACHER:
                authorities.add(new SimpleGrantedAuthority("PERM_EVALUATE_KINDER"));
                authorities.add(new SimpleGrantedAuthority("PERM_VIEW_KINDER_STUDENTS"));
                authorities.add(new SimpleGrantedAuthority("PERM_CREATE_ACTIVITIES"));
                break;
                
            case PSYCHOLOGIST:
                authorities.add(new SimpleGrantedAuthority("PERM_CONDUCT_INTERVIEWS"));
                authorities.add(new SimpleGrantedAuthority("PERM_PSYCHOLOGICAL_EVALUATION"));
                authorities.add(new SimpleGrantedAuthority("PERM_VIEW_PSYCHOLOGICAL_REPORTS"));
                break;
                
            case SUPPORT_STAFF:
                authorities.add(new SimpleGrantedAuthority("PERM_ADMINISTRATIVE_TASKS"));
                authorities.add(new SimpleGrantedAuthority("PERM_VIEW_SCHEDULES"));
                break;
                
            case USER:
                authorities.add(new SimpleGrantedAuthority("PERM_VIEW_PROFILE"));
                break;
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.isActive())
                .build();
    }

    // Método auxiliar para obtener el usuario completo por username
    public Usuario findUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    // Método auxiliar para obtener el usuario completo por email
    public Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}