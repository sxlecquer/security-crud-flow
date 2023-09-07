package com.example.client.repository;

import com.example.client.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    @Query("select l.password from Lecturer l where l.email=?1")
    String findPasswordByEmail(String email);
}
