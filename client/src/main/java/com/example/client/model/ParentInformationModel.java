package com.example.client.model;

import com.example.client.order.CorrectInfo;
import com.example.client.order.NotEmptyField;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@GroupSequence({NotEmptyField.class, CorrectInfo.class, ParentInformationModel.class})
public class ParentInformationModel {
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
