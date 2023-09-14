package com.example.client.model;

import com.example.client.annotation.FieldsValueMatch;
import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GroupSequence({NotEmptyField.class, CorrectInfo.class, ResetPasswordModel.class})
@FieldsValueMatch(
        message = "Password mismatch",
        field = "newPassword",
        fieldMatch = "confirmNewPassword",
        groups = CorrectInfo.class
)
public class ResetPasswordModel {
    @NotEmpty(message = "Password should not be empty", groups = NotEmptyField.class)
    @Size(min = 6, message = "Password is too simple", groups = CorrectInfo.class)
    private String newPassword;

    @NotEmpty(message = "Confirm password should not be empty", groups = NotEmptyField.class)
    private String confirmNewPassword;
}
