package com.example.client.repository;

import com.example.client.entity.Curator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface CuratorRepository extends JpaRepository<Curator, Long> {
    @Query("select c.password from Curator c where c.email=?1")
    String findPasswordByEmail(String email);

    Curator findByEmail(String email);

    @Modifying
    @Query("delete from Curator c where c.curatorId = :id")
    void deleteById(@Param("id") @NonNull Long id);
}