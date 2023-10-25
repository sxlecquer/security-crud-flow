package com.example.client.model;

import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GroupSequence({NotEmptyField.class, CorrectInfo.class, BasicInformationModel.class})
public class BasicInformationModel {
    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Size(min = 2, max = 15, message = "First name is too long or too short", groups = CorrectInfo.class)
    private String firstName;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Size(min = 2, max = 20, message = "Last name is too long or too short", groups = CorrectInfo.class)
    private String lastName;

    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Email(message = "Email address is incorrect", groups = CorrectInfo.class)
    private String email;

    private String role;
}
