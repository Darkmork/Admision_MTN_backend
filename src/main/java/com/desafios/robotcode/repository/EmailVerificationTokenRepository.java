package com.desafios.robotcode.repository;

import com.desafios.robotcode.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    
    Optional<EmailVerificationToken> findByToken(String token);
    
    Optional<EmailVerificationToken> findByEmailAndToken(String email, String token);
    
    Optional<EmailVerificationToken> findByEmail(String email);
    
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.email = :email AND t.isUsed = false AND t.isExpired = false")
    Optional<EmailVerificationToken> findValidTokenByEmail(@Param("email") String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIsUsedFalseAndIsExpiredFalse(String email);
    
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerificationToken t SET t.isExpired = true WHERE t.expiresAt < :now")
    int markExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.isUsed = true OR t.isExpired = true OR t.expiresAt < :cutoffTime")
    int deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerificationToken t SET t.isExpired = true WHERE t.email = :email AND t.isUsed = false")
    int invalidateTokensByEmail(@Param("email") String email);
}