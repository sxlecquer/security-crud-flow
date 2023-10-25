package com.example.client.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    @NotEmpty(message = "Fill in this field")
    private String email;

    @NotEmpty(message = "Fill in this field")
    private String password;
}
