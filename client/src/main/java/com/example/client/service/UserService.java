package com.example.client.service;

import com.example.client.entity.PasswordToken;
import com.example.client.entity.User;
import com.example.client.token.TokenState;

public interface UserService {
    void savePasswordToken(User user, String token);

    TokenState validatePasswordToken(String token);

    PasswordToken findPasswordToken(String token);

    User getUserByPasswordToken(PasswordToken passwordToken);

    void changePassword(User user, String newPassword);
}
