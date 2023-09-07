package com.example.client.service;

import com.example.client.entity.Student;
import com.example.client.entity.VerificationToken;
import com.example.client.model.StudentModel;

import java.util.List;

public interface StudentService {
    Student register(StudentModel studentModel);

    List<Student> findAll();

    String findPasswordByEmail(String email);

    void saveVerificationToken(Student student, String token);

    String validateVerificationToken(String token);

    String sendNewVerificationToken(String oldToken);

    VerificationToken findVerificationTokenByStudent(Student student);

    void deleteById(int i);
}
