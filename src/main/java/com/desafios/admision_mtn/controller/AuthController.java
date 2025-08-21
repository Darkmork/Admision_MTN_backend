package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.AuthResponse;
import com.desafios.admision_mtn.dto.LoginRequest;
import com.desafios.admision_mtn.dto.RegisterRequest;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.UserService;
import com.desafios.admision_mtn.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;
            String token = jwtUtil.generateToken(userDetails);
            
            return ResponseEntity.ok(AuthResponse.success(
                token, 
                user.getEmail(), 
                user.getFirstName(), 
                user.getLastName(), 
                user.getRole().name()
            ));
            
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(
                AuthResponse.error("Credenciales inválidas")
            );
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
            String token = jwtUtil.generateToken(userDetails);
            
            return ResponseEntity.ok(AuthResponse.success(
                token, 
                user.getEmail(), 
                user.getFirstName(), 
                user.getLastName(), 
                user.getRole().name()
            ));
            
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(
                AuthResponse.error(e.getMessage())
            );
        }
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}