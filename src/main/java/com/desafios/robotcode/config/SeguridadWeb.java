package com.desafios.robotcode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SeguridadWeb {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // forma moderna de desactivar CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // permite todo sin login
                )
                .httpBasic(Customizer.withDefaults()) // opcional: permite login básico si quisieras probar algo
                .build();
    }
}
