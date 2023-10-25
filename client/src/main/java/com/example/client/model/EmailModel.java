package com.example.client.model;

import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@GroupSequence({NotEmptyField.class, CorrectInfo.class, EmailModel.class})
public class EmailModel {
    @NotEmpty(message = "Fill in this field", groups = NotEmptyField.class)
    @Email(message = "Email address is incorrect", groups = CorrectInfo.class)
    private String email;
}
