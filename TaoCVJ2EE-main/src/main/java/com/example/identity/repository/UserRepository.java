package com.example.identity.repository;

import com.example.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE FUNCTION('YEAR', u.createdAt) = :year AND FUNCTION('MONTH', u.createdAt) = :month")
    long countByCreatedAtMonth(@Param("year") int year, @Param("month") int month);


}
