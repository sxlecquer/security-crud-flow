package com.example.client.repository;

import com.example.client.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("select s.password from Student s where s.email=?1")
    String findPasswordByEmail(String email);

    Student findByEmail(String email);

    @Modifying
    @Query("delete from Student s where s.studentId = :id")
    void deleteById(@Param("id") Integer id);
}
