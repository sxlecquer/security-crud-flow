package com.example.client.service;

import com.example.client.entity.Curator;
import com.example.client.entity.Student;
import com.example.client.model.BasicInformationModel;

import java.util.List;

public interface CuratorService {
    int STUDENT_LIMIT = 5;

    String findPasswordByEmail(String email);

    List<Curator> findAll();

    Curator assignCurator(Student student);

    void fillCuratorTable();

    Curator findByEmail(String email);

    Curator findById(Long id);

    void changePassword(Curator curator, String newPassword);

    void saveCuratorChanges(Curator curator, BasicInformationModel basicInformationModel);

    void deleteById(Long id);
}
