package com.example.client.service.impl;

import com.example.client.entity.Lecturer;
import com.example.client.model.BasicInformationModel;
import com.example.client.repository.LecturerRepository;
import com.example.client.service.LecturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LecturerServiceImpl implements LecturerService {
    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String findPasswordByEmail(String email) {
        return lecturerRepository.findPasswordByEmail(email);
    }

    @Override
    public List<Lecturer> findAll() {
        return lecturerRepository.findAll();
    }

    @Override
    public void fillLecturerTable() {
        /*Lecturer lecturer1 = new Lecturer();
        lecturer1.setFirstName("Kimberly");
        lecturer1.setLastName("Harris");
        lecturer1.setEmail(lecturer1.getFirstName().toLowerCase(Locale.ROOT) + "." + lecturer1.getLastName().toLowerCase(Locale.ROOT) + "@univ.lec.com");
        lecturer1.setPassword(passwordEncoder.encode("Harris123"));
        
        Lecturer lecturer2 = new Lecturer();
        lecturer2.setFirstName("Nancy");
        lecturer2.setLastName("Torres");
        lecturer2.setEmail(lecturer2.getFirstName().toLowerCase(Locale.ROOT) + "." + lecturer2.getLastName().toLowerCase(Locale.ROOT) + "@univ.lec.com");
        lecturer2.setPassword(passwordEncoder.encode("Torres123"));

        Lecturer lecturer3 = new Lecturer();
        lecturer3.setFirstName("Daniel");
        lecturer3.setLastName("Roberts");
        lecturer3.setEmail(lecturer3.getFirstName().toLowerCase(Locale.ROOT) + "." + lecturer3.getLastName().toLowerCase(Locale.ROOT) + "@univ.lec.com");
        lecturer3.setPassword(passwordEncoder.encode("Roberts123"));

        Lecturer lecturer4 = new Lecturer();
        lecturer4.setFirstName("Jessie");
        lecturer4.setLastName("Hill");
        lecturer4.setEmail(lecturer4.getFirstName().toLowerCase(Locale.ROOT) + "." + lecturer4.getLastName().toLowerCase(Locale.ROOT) + "@univ.lec.com");
        lecturer4.setPassword(passwordEncoder.encode("Hill123"));

        Lecturer lecturer5 = new Lecturer();
        lecturer5.setFirstName("Alfred");
        lecturer5.setLastName("Campbell");
        lecturer5.setEmail(lecturer5.getFirstName().toLowerCase(Locale.ROOT) + "." + lecturer5.getLastName().toLowerCase(Locale.ROOT) + "@univ.lec.com");
        lecturer5.setPassword(passwordEncoder.encode("Campbell123"));

        lecturerRepository.saveAll(List.of(lecturer1, lecturer2, lecturer3, lecturer4, lecturer5));*/
    }

    @Override
    public Lecturer findByEmail(String email) {
        return lecturerRepository.findByEmail(email);
    }

    @Override
    public Lecturer findById(int id) {
        Optional<Lecturer> lecturer = lecturerRepository.findById((long) id);
        return lecturer.orElse(null);
    }

    @Override
    public void changePassword(Lecturer lecturer, String newPassword) {
        lecturer.setPassword(passwordEncoder.encode(newPassword));
        lecturerRepository.save(lecturer);
    }

    @Override
    public void saveLecturerChanges(Lecturer lecturer, BasicInformationModel basicInformationModel) {
        lecturer.setFirstName(basicInformationModel.getFirstName());
        lecturer.setLastName(basicInformationModel.getLastName());
        lecturerRepository.save(lecturer);
    }
}
