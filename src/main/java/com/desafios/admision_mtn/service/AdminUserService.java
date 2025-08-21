package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.CreateUserRequest;
import com.desafios.admision_mtn.dto.UpdateUserRequest;
import com.desafios.admision_mtn.dto.UserResponse;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.repository.EvaluationRepository;
import com.desafios.admision_mtn.util.RutUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    public Page<UserResponse> getAllUsers(String search, User.UserRole role, Boolean active, Pageable pageable) {
        Specification<User> spec = Specification.where(null);
        
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), searchTerm),
                cb.like(cb.lower(root.get("lastName")), searchTerm),
                cb.like(cb.lower(root.get("email")), searchTerm),
                cb.like(cb.lower(root.get("rut")), searchTerm)
            ));
        }
        
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        
        return userRepository.findAll(spec, pageable)
                .map(UserResponse::fromUser);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return UserResponse.fromUser(user);
    }
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Validar que no exista el email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con este email");
        }
        
        // Validar que no exista el RUT
        String formattedRut = RutUtil.formatRut(request.getRut());
        if (userRepository.existsByRut(formattedRut)) {
            throw new RuntimeException("Ya existe un usuario con este RUT");
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRut(formattedRut);
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setEducationalLevel(request.getEducationalLevel());
        user.setSubject(request.getSubject());
        user.setEmailVerified(true); // Los usuarios creados por admin están pre-verificados
        user.setActive(true);
        
        // Generar contraseña temporal si no se proporciona
        String password = request.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = generateTemporaryPassword();
        }
        user.setPassword(passwordEncoder.encode(password));
        
        User savedUser = userRepository.save(user);
        
        // Enviar email de bienvenida si está habilitado
        if (request.getSendWelcomeEmail()) {
            sendWelcomeEmail(savedUser, password);
        }
        
        return UserResponse.fromUser(savedUser);
    }
    
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar email único si cambió
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Ya existe un usuario con este email");
            }
            user.setEmail(request.getEmail());
        }
        
        // Validar RUT único si cambió
        String formattedRut = RutUtil.formatRut(request.getRut());
        if (!user.getRut().equals(formattedRut)) {
            if (userRepository.existsByRut(formattedRut)) {
                throw new RuntimeException("Ya existe un usuario con este RUT");
            }
            user.setRut(formattedRut);
        }
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getEducationalLevel() != null) {
            user.setEducationalLevel(request.getEducationalLevel());
        }
        if (request.getSubject() != null) {
            user.setSubject(request.getSubject());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        
        User savedUser = userRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }
    
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole() == User.UserRole.ADMIN && user.getActive()) {
            // Verificar que no sea el último admin activo
            long activeAdminCount = userRepository.countByRoleAndActiveTrue(User.UserRole.ADMIN);
            if (activeAdminCount <= 1) {
                throw new RuntimeException("No se puede desactivar el último administrador activo del sistema");
            }
        }
        
        user.setActive(false);
        userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole() == User.UserRole.ADMIN) {
            // Verificar que no sea el último admin (contar todos los admin, no solo activos)
            long totalAdminCount = userRepository.countByRole(User.UserRole.ADMIN);
            if (totalAdminCount <= 1) {
                throw new RuntimeException("No se puede eliminar el último administrador del sistema");
            }
        }
        
        // Verificar si el usuario tiene evaluaciones asociadas
        long evaluationCount = evaluationRepository.countByEvaluatorId(id);
        if (evaluationCount > 0) {
            throw new RuntimeException(
                String.format("No se puede eliminar este usuario porque tiene %d evaluación(es) asociada(s). " +
                             "Para proteger la integridad de los datos del proceso de admisión, " +
                             "recomendamos desactivar el usuario en lugar de eliminarlo permanentemente.", 
                             evaluationCount)
            );
        }
        
        // Eliminar permanentemente el usuario de la base de datos
        userRepository.delete(user);
    }
    
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }
    
    @Transactional
    public void resetUserPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String newPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Enviar email con nueva contraseña
        sendPasswordResetEmail(user, newPassword);
    }
    
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Estadísticas generales
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByActiveTrue());
        stats.put("inactiveUsers", userRepository.countByActiveFalse());
        stats.put("verifiedUsers", userRepository.countByEmailVerifiedTrue());
        stats.put("unverifiedUsers", userRepository.countByEmailVerifiedFalse());
        
        // Estadísticas por rol
        Map<String, Long> roleStats = new HashMap<>();
        for (User.UserRole role : User.UserRole.values()) {
            roleStats.put(role.name(), userRepository.countByRole(role));
        }
        stats.put("roleStats", roleStats);
        
        return stats;
    }
    
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    private void sendWelcomeEmail(User user, String password) {
        try {
            emailService.sendWelcomeEmailWithCredentials(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                password,
                user.getRole().getDisplayName()
            );
        } catch (Exception e) {
            // Log error but don't fail user creation
            System.err.println("Error sending welcome email: " + e.getMessage());
        }
    }
    
    private void sendPasswordResetEmail(User user, String newPassword) {
        try {
            emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                newPassword
            );
        } catch (Exception e) {
            // Log error but don't fail password reset
            System.err.println("Error sending password reset email: " + e.getMessage());
        }
    }
}