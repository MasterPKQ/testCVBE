package com.example.identity.repository;

import com.example.identity.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findProfileByUser_Username(String username);
    @Query("SELECT COUNT(p) FROM Profile p")
    long countProfiles();
}
