package com.example.client.model;

import com.example.client.annotation.FieldsValueMatch;
import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GroupSequence({NotEmptyField.class, CorrectInfo.class, StudentModel.class}) // class order matters
@FieldsValueMatch(
        message = "Password mismatch",
        field = "password",
        fieldMatch = "confirmPassword",
        groups = CorrectInfo.class
)
public class StudentModel {
    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Size(min = 2, max = 15, message = "First name is too long or too short", groups = CorrectInfo.class)
    private String firstName;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Size(min = 2, max = 20, message = "Last name is too long or too short", groups = CorrectInfo.class)
    private String lastName;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Email(message = "Email address is incorrect", groups = CorrectInfo.class)
    private String email;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Pattern(message = "Password must meet the requirements", regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$", groups = CorrectInfo.class)
    private String password;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    private String confirmPassword;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Size(min = 2, max = 15, message = "First name is too long or too short", groups = CorrectInfo.class)
    private String parentFirst;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Size(min = 2, max = 20, message = "Last name is too long or too short", groups = CorrectInfo.class)
    private String parentLast;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Pattern(message = "Incorrect mobile phone format", regexp = "\\+\\d{1,4}-\\d{3}-\\d{3}-\\d{2,}", groups = CorrectInfo.class)
    private String parentMobile;
}
