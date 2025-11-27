package com.example.identity.repository;

import com.example.identity.entity.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findAllByUserUsername(String username);
}
