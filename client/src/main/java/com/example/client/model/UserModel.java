package com.example.client.model;

import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GroupSequence({NotEmptyField.class, CorrectInfo.class, UserModel.class})
public class UserModel {
    @NotEmpty(message = "Email should not be empty", groups = NotEmptyField.class)
    @Email(message = "Email address is incorrect", groups = CorrectInfo.class)
    private String email;
    @NotEmpty(message = "Password should not be empty", groups = NotEmptyField.class)
    private String password;
}
