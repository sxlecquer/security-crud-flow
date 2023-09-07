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
    @NotEmpty(message = "First name should not be empty", groups = NotEmptyField.class)
    @Size(min = 2, max = 15, message = "First name is too long or too short", groups = CorrectInfo.class)
    private String firstName;

    @NotEmpty(message = "Last name should not be empty", groups = NotEmptyField.class)
    @Size(min = 2, max = 20, message = "Last name is too long or too short", groups = CorrectInfo.class)
    private String lastName;

    @NotEmpty(message = "Email should not be empty", groups = NotEmptyField.class)
    @Email(message = "Email address is incorrect", groups = CorrectInfo.class)
    private String email;

    @NotEmpty(message = "Password should not be empty", groups = NotEmptyField.class)
    @Size(min = 6, message = "Password is too simple", groups = CorrectInfo.class)
    private String password;

    @NotEmpty(message = "Confirm password should not be empty", groups = NotEmptyField.class)
    private String confirmPassword;

    @NotEmpty(message = "First name should not be empty", groups = NotEmptyField.class)
    @Size(min = 2, max = 15, message = "First name is too long or too short", groups = CorrectInfo.class)
    private String parentFirst;

    @NotEmpty(message = "Last name should not be empty", groups = NotEmptyField.class)
    @Size(min = 2, max = 20, message = "Last name is too long or too short", groups = CorrectInfo.class)
    private String parentLast;

    @NotEmpty(message = "Phone number should not be empty", groups = NotEmptyField.class)
    @Pattern(message = "Incorrect mobile phone format", regexp = "\\+\\d{1,4}-\\d{3}-\\d{3}-\\d{2,}", groups = CorrectInfo.class)
    private String parentMobile;
}
