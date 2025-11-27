package com.example.identity.repository;

import com.example.identity.entity.CVSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVSectionRepository extends JpaRepository<CVSection, Long> {
    List<CVSection> findAllByCvId(Long cvId);
}
