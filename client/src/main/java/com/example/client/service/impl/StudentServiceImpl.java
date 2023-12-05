package com.example.client.service.impl;

import com.example.client.entity.*;
import com.example.client.model.BasicInformationModel;
import com.example.client.model.ParentInformationModel;
import com.example.client.model.StudentModel;
import com.example.client.repository.StudentRepository;
import com.example.client.repository.VerificationTokenRepository;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import com.example.client.token.TokenState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Autowired
    private EmailSenderService emailSenderService;

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
        VerificationToken verificationToken = verificationTokenRepository.findByStudent(student);
        if(verificationToken == null) {
            verificationToken = new VerificationToken(student, token);
        } else {
            verificationToken.setToken(token);
            verificationToken.setExpirationTime(verificationToken.calculateExpirationTime());
        }
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public TokenState validateVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if(verificationToken == null)
            return TokenState.WRONG;
        if(verificationToken.getExpirationTime().getTime() <= new Date().getTime()) {
            return TokenState.EXPIRED;
        }
        Student student = verificationToken.getStudent();
        student.setEnabled(true);
        studentRepository.save(student);
        return TokenState.VALID;
    }

    @Override
    public String sendNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        if(verificationToken != null) {
            String newToken = String.format(Locale.ROOT, "%06d", new Random().nextInt(1000000));
            verificationToken.setToken(newToken);
            verificationToken.setExpirationTime(verificationToken.calculateExpirationTime());
            verificationTokenRepository.save(verificationToken);
            emailSenderService.sendEmail(verificationToken.getStudent().getEmail(), "Verification code", "Verification code to verify your email:\n" + newToken);
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
    public void deleteById(int id) {
        studentRepository.deleteById((long) id);
    }

    @Override
    public Student findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    @Override
    public Student findById(int id) {
        Optional<Student> student = studentRepository.findById((long) id);
        return student.orElse(null);
    }

    @Override
    public void changePassword(Student student, String newPassword) {
        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);
    }

    @Override
    public void saveStudentChanges(Student student, BasicInformationModel basicInformationModel) {
        student.setFirstName(basicInformationModel.getFirstName());
        student.setLastName(basicInformationModel.getLastName());
        if(!student.getEmail().equals(basicInformationModel.getEmail())) {
            student.setEmail(basicInformationModel.getEmail());
            student.setEnabled(false);
        }
        studentRepository.save(student);
    }

    @Override
    public void saveParentChanges(Student student, ParentInformationModel parentInformationModel) {
        student.getParent().setFirstName(parentInformationModel.getParentFirst());
        student.getParent().setLastName(parentInformationModel.getParentLast());
        student.getParent().setPhoneNumber(parentInformationModel.getParentMobile());
        studentRepository.save(student);
    }

    @Override
    public void save(Student student) {
        studentRepository.save(student);
    }
}
