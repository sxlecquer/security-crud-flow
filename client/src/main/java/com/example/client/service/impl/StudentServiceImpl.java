package com.example.client.service.impl;

import com.example.client.entity.*;
import com.example.client.model.StudentModel;
import com.example.client.repository.StudentRepository;
import com.example.client.repository.VerificationTokenRepository;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CuratorService curatorService;
    @Autowired
    private LecturerService lecturerService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Override
    public Student register(StudentModel studentModel) {
        Parent parent = new Parent();
        parent.setFirstName(studentModel.getParentFirst());
        parent.setLastName(studentModel.getParentLast());
        parent.setPhoneNumber(studentModel.getParentMobile());
        Student student = new Student();
        student.setParent(parent);
        student.setFirstName(studentModel.getFirstName());
        student.setLastName(studentModel.getLastName());
        student.setEmail(studentModel.getEmail());
        student.setPassword(passwordEncoder.encode(studentModel.getPassword()));

        student.setLecturers(lecturerService.findAll());
//        lecturerService.addStudent(student);
        student.setCurator(curatorService.assignCurator(student));
        parent.setStudent(student);

        studentRepository.save(student);
        return student;
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public String findPasswordByEmail(String email) {
        return studentRepository.findPasswordByEmail(email);
    }

    @Override
    public void saveVerificationToken(Student student, String token) {
        VerificationToken verificationToken = new VerificationToken(student, token);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if(verificationToken == null)
            return "wrong";
        if(verificationToken.getExpirationTime().getTime() <= new Date().getTime()) {
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }
        Student student = verificationToken.getStudent();
        student.setEnabled(true);
        studentRepository.save(student);
        return "valid";
    }

    @Override
    public String sendNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        if(verificationToken != null) {
            String newToken = String.format(Locale.ROOT, "%06d", new Random().nextInt(1000000));
            verificationToken.setToken(newToken);
            verificationToken.setExpirationTime(verificationToken.calculateExpirationTime());
            verificationTokenRepository.save(verificationToken);
            log.info("Verification code to verify your email:\n{}", newToken);
        } else {
            log.warn("Can't resend verification code because there is no old token ({}) in verification token repository", oldToken);
        }
        return verificationToken != null ? verificationToken.getToken() : "";
    }

    @Override
    public VerificationToken findVerificationTokenByStudent(Student student) {
        return verificationTokenRepository.findByStudent(student);
    }

    @Override
    public void deleteById(int i) {
        studentRepository.deleteById((long) i);
    }
}
