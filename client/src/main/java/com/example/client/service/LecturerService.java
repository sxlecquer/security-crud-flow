package com.example.client.service;

import com.example.client.entity.Lecturer;
import com.example.client.entity.Student;

import java.util.List;

public interface LecturerService {
    String findPasswordByEmail(String email);

    List<Lecturer> findAll();

//    void addStudent(Student student);

    void fillLecturerTable();
}
