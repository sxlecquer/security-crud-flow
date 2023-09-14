package com.example.client.service;

import com.example.client.entity.Curator;
import com.example.client.entity.Student;

import java.util.List;

public interface CuratorService {
    int STUDENT_LIMIT = 4;
    String findPasswordByEmail(String email);

    List<Curator> findAll();

    Curator assignCurator(Student student);

    void fillCuratorTable();

    Curator findByEmail(String email);

    Curator findById(int id);

    void changePassword(Curator curator, String newPassword);
}
