package com.example.client.model;

import com.example.client.annotation.FieldsValueMatch;
import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GroupSequence({NotEmptyField.class, CorrectInfo.class, ChangePasswordModel.class})
@FieldsValueMatch(
        message = "Password mismatch",
        field = "newPassword",
        fieldMatch = "confirmNewPassword",
        groups = CorrectInfo.class
)
public class ChangePasswordModel {
    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    private String oldPassword;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Pattern(message = "Password must meet the requirements", regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$", groups = CorrectInfo.class)
    private String newPassword;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    private String confirmNewPassword;
}
