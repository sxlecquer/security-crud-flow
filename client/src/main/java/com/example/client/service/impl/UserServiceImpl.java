package com.example.client.service.impl;

import com.example.client.entity.*;
import com.example.client.model.BasicInformationModel;
import com.example.client.repository.PasswordTokenRepository;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import com.example.client.service.UserService;
import com.example.client.token.TokenState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CuratorService curatorService;

    @Autowired
    private LecturerService lecturerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void savePasswordToken(User user, String token) {
        PasswordToken passwordToken = new PasswordToken(user, token);
        passwordTokenRepository.save(passwordToken);
    }

    @Override
    public TokenState validatePasswordToken(String token) {
        PasswordToken passwordToken = passwordTokenRepository.findByToken(token);
        if(passwordToken == null) {
            return TokenState.WRONG;
        } else if(passwordToken.getExpirationTime().getTime() <= new Date().getTime()) {
            return TokenState.EXPIRED;
        }
        return TokenState.VALID;
    }

    @Override
    public String sendNewPasswordToken(String oldToken) {
        PasswordToken passwordToken = passwordTokenRepository.findByToken(oldToken);
        if(passwordToken != null) {
            String newToken = UUID.randomUUID().toString();
            passwordToken.setToken(newToken);
            passwordToken.setExpirationTime(passwordToken.calculateExpirationTime());
            passwordTokenRepository.save(passwordToken);
        } else {
            log.warn("Can't resend password token because there is no old token ({}) in password token repository", oldToken);
        }
        return passwordToken != null ? passwordToken.getToken() : "";
    }

    @Override
    public void changePassword(User user, String newPassword) {
        if(user instanceof Student) {
            studentService.changePassword((Student) user, newPassword);
        } else if(user instanceof Curator) {
            curatorService.changePassword((Curator) user, newPassword);
        } else if(user instanceof Lecturer) {
            lecturerService.changePassword((Lecturer) user, newPassword);
        }
    }

    @Override
    public PasswordToken findPasswordToken(String token) {
        return passwordTokenRepository.findByToken(token);
    }

    @Override
    public User findUserByPasswordToken(PasswordToken passwordToken) {
        String userAndId = passwordToken.getUserAndId();
        String[] array = userAndId.split(" ");
        String user = array[0];
        int id = Integer.parseInt(array[1]);
        return switch(user) {
            case "Student" -> studentService.findById(id);
            case "Curator" -> curatorService.findById(id);
            case "Lecturer" -> lecturerService.findById(id);
            default -> null;
        };
    }

    @Override
    public User findUserByEmailAndPassword(String email, String password) {
        Student student = studentService.findByEmail(email);
        if(student != null && passwordEncoder.matches(password, student.getPassword()))
            return student;
        Curator curator = curatorService.findByEmail(email);
        if(curator != null && passwordEncoder.matches(password, curator.getPassword()))
            return curator;
        Lecturer lecturer = lecturerService.findByEmail(email);
        if(lecturer != null && passwordEncoder.matches(password, lecturer.getPassword()))
            return lecturer;
        return null;
    }

    @Override
    public void saveUserChanges(User user, BasicInformationModel basicInformationModel) {
        if(user instanceof Student) {
            studentService.saveStudentChanges((Student) user, basicInformationModel);
        } else if(user instanceof Curator) {
            curatorService.saveCuratorChanges((Curator) user, basicInformationModel);
        } else if(user instanceof Lecturer) {
            lecturerService.saveLecturerChanges((Lecturer) user, basicInformationModel);
        }
    }
}
