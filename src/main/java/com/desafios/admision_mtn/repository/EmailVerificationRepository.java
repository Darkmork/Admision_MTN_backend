package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    Optional<EmailVerification> findByEmailAndCodeAndUsedFalse(String email, String code);
    
    Optional<EmailVerification> findByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, EmailVerification.VerificationType type);
    
    void deleteByEmailAndType(String email, EmailVerification.VerificationType type);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}