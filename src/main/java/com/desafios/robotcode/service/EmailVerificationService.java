package com.desafios.robotcode.service;

import com.desafios.robotcode.model.EmailVerificationToken;
import com.desafios.robotcode.model.Usuario;
import com.desafios.robotcode.repository.EmailVerificationTokenRepository;
import com.desafios.robotcode.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class EmailVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    
    private static final Pattern MTN_EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@(mtn\\.cl|alumnos\\.mtn\\.cl)$");
    
    private final EmailVerificationTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    
    @Autowired
    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UsuarioRepository usuarioRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Valida si un email pertenece a los dominios MTN permitidos
     */
    public boolean isValidMTNEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return MTN_EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    /**
     * Inicia el proceso de registro con verificación por email
     */
    @Transactional
    public void initiateRegistration(String username, String email, String password) {
        // Validar email MTN
        if (!isValidMTNEmail(email)) {
            throw new IllegalArgumentException("Solo se permiten correos con dominios @mtn.cl o @alumnos.mtn.cl");
        }
        
        // Verificar si ya existe un usuario con ese email o username
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con este correo electrónico");
        }
        
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con este nombre de usuario");
        }
        
        // Invalidar tokens previos para este email
        tokenRepository.invalidateTokensByEmail(email);
        
        // Generar código de verificación
        String verificationCode = generateVerificationCode();
        
        // Hashear la contraseña
        String hashedPassword = passwordEncoder.encode(password);
        
        // Crear y guardar token de verificación
        EmailVerificationToken token = new EmailVerificationToken(
            verificationCode, email, username, hashedPassword
        );
        
        tokenRepository.save(token);
        
        // Enviar email de verificación
        emailService.sendVerificationEmail(email, username, verificationCode);
        
        logger.info("Verification email sent for user: {} with email: {}", username, email);
    }
    
    /**
     * Verifica el código y completa el registro del usuario
     */
    @Transactional
    public Usuario verifyEmailAndCompleteRegistration(String email, String verificationCode) {
        // Buscar token válido
        Optional<EmailVerificationToken> tokenOpt = tokenRepository
            .findByEmailAndToken(email, verificationCode);
        
        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Código de verificación inválido");
        }
        
        EmailVerificationToken token = tokenOpt.get();
        
        // Verificar si el token es válido
        if (!token.isTokenValid()) {
            if (token.isTokenExpired()) {
                throw new IllegalArgumentException("El código de verificación ha expirado. Solicita uno nuevo.");
            } else {
                throw new IllegalArgumentException("El código de verificación ya ha sido utilizado");
            }
        }
        
        // Verificar nuevamente que no exista el usuario (por si acaso)
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con este correo electrónico");
        }
        
        if (usuarioRepository.findByUsername(token.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con este nombre de usuario");
        }
        
        // Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(token.getUsername());
        usuario.setEmail(email);
        usuario.setPassword(token.getPasswordHash()); // Ya está hasheada
        usuario.setEmailVerified(true);
        usuario.setFechaRegistro(LocalDateTime.now(ZoneOffset.UTC));
        
        Usuario savedUser = usuarioRepository.save(usuario);
        
        // Marcar token como usado
        token.markAsUsed();
        tokenRepository.save(token);
        
        // Enviar email de bienvenida
        emailService.sendWelcomeEmail(email, token.getUsername());
        
        logger.info("User registration completed successfully for: {} with email: {}", 
                   token.getUsername(), email);
        
        return savedUser;
    }
    
    /**
     * Reenvía el código de verificación
     */
    @Transactional
    public void resendVerificationCode(String email) {
        if (!isValidMTNEmail(email)) {
            throw new IllegalArgumentException("Solo se permiten correos con dominios @mtn.cl o @alumnos.mtn.cl");
        }
        
        // Verificar si ya existe un usuario con ese email
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta verificada con este correo electrónico");
        }
        
        // Buscar token pendiente
        Optional<EmailVerificationToken> existingTokenOpt = 
            tokenRepository.findValidTokenByEmail(email);
        
        if (existingTokenOpt.isEmpty()) {
            throw new IllegalArgumentException("No hay un proceso de registro pendiente para este correo");
        }
        
        EmailVerificationToken existingToken = existingTokenOpt.get();
        
        // Invalidar token anterior
        existingToken.markAsExpired();
        tokenRepository.save(existingToken);
        
        // Crear nuevo token
        String newVerificationCode = generateVerificationCode();
        EmailVerificationToken newToken = new EmailVerificationToken(
            newVerificationCode, email, existingToken.getUsername(), existingToken.getPasswordHash()
        );
        
        tokenRepository.save(newToken);
        
        // Reenviar email
        emailService.sendVerificationEmail(email, existingToken.getUsername(), newVerificationCode);
        
        logger.info("Verification code resent for email: {}", email);
    }
    
    /**
     * Genera un código de verificación de 6 dígitos
     */
    private String generateVerificationCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * Limpieza automática de tokens expirados (se ejecuta cada hora)
     */
    @Scheduled(fixedRate = 3600000) // 1 hora
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(24);
        int deletedCount = tokenRepository.deleteExpiredTokens(cutoffTime);
        
        if (deletedCount > 0) {
            logger.info("Cleaned up {} expired verification tokens", deletedCount);
        }
    }
    
    /**
     * Marca tokens expirados (se ejecuta cada 10 minutos)
     */
    @Scheduled(fixedRate = 600000) // 10 minutos
    @Transactional
    public void markExpiredTokens() {
        int markedCount = tokenRepository.markExpiredTokens(LocalDateTime.now(ZoneOffset.UTC));
        
        if (markedCount > 0) {
            logger.debug("Marked {} tokens as expired", markedCount);
        }
    }
}