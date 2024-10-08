package com.example.client.service;

import com.example.client.entity.Student;
import com.example.client.entity.VerificationToken;
import com.example.client.model.BasicInformationModel;
import com.example.client.model.ParentInformationModel;
import com.example.client.model.StudentModel;
import com.example.client.token.TokenState;

import java.util.List;

public interface StudentService {
    Student register(StudentModel studentModel);

    List<Student> findAll();

    String findPasswordByEmail(String email);

    void saveVerificationToken(Student student, String token);

    TokenState validateVerificationToken(String token);

    String sendNewVerificationToken(String oldToken);

    VerificationToken findVerificationTokenByStudent(Student student);

    void deleteById(Long id);

    Student findByEmail(String email);

    Student findById(Long id);

    void changePassword(Student student, String newPassword);

    void saveStudentChanges(Student student, BasicInformationModel basicInformationModel);

    void saveParentChanges(Student student, ParentInformationModel parentInformationModel);

    void save(Student student);
}
