package com.aureon.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aureon.backend.entity.OtpToken;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllForEmail(String email);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :threshold")
    void deleteExpiredOtps(LocalDateTime threshold);
}
