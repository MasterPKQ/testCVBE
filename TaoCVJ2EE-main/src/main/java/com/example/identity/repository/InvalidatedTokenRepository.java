package com.example.identity.repository;

import com.example.identity.entity.InvalidatedToken;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    boolean existsById(String token);
    @Modifying
    @Transactional
    @Query("DELETE FROM InvalidatedToken t WHERE t.expirationTime <= :threshold")
    void deleteExpiredTokens(@Param("threshold") Date threshold);
}
