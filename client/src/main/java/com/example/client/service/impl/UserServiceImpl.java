package com.example.client.service.impl;

import com.example.client.entity.*;
import com.example.client.repository.PasswordTokenRepository;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import com.example.client.service.UserService;
import com.example.client.token.TokenState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CuratorService curatorService;

    @Autowired
    private LecturerService lecturerService;

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
            passwordTokenRepository.delete(passwordToken); // MAY CAUSE ERRORS DURING RESENDING PASSWORD TOKEN
            return TokenState.EXPIRED;
        }
        return TokenState.VALID;
    }

    @Override
    public PasswordToken findPasswordToken(String token) {
        return passwordTokenRepository.findByToken(token);
    }

    @Override
    public User getUserByPasswordToken(PasswordToken passwordToken) {
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
    public void changePassword(User user, String newPassword) {
        if(user instanceof Student) {
            studentService.changePassword((Student) user, newPassword);
        } else if(user instanceof Curator) {
            curatorService.changePassword((Curator) user, newPassword);
        } else if(user instanceof Lecturer) {
            lecturerService.changePassword((Lecturer) user, newPassword);
        }
    }
}
