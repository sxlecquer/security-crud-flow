package com.example.client.repository;

import com.example.client.entity.Curator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CuratorRepository extends JpaRepository<Curator, Long> {
    @Query("select c.password from Curator c where c.email=?1")
    String findPasswordByEmail(String email);
}