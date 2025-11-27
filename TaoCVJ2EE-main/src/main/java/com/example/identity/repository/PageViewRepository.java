package com.example.identity.repository;

import com.example.identity.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {

    @Query("SELECT COUNT(p) FROM PageView p WHERE p.page = :page")
    long countByPage(String page);
}
