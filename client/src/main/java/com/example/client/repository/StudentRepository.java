package com.example.client.repository;

import com.example.client.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("select s.password from Student s where s.email=?1")
    String findPasswordByEmail(String email);

    Student findByEmail(String email);
}
