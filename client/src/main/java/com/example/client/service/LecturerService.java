package com.example.client.service;

import com.example.client.entity.Lecturer;
import com.example.client.model.BasicInformationModel;

import java.util.List;

public interface LecturerService {
    String findPasswordByEmail(String email);

    List<Lecturer> findAll();

    void fillLecturerTable();

    Lecturer findByEmail(String email);

    Lecturer findById(int id);

    void changePassword(Lecturer lecturer, String newPassword);

    void saveLecturerChanges(Lecturer lecturer, BasicInformationModel basicInformationModel);
}
