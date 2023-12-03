package com.example.client.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationTokenModel {
    @NotEmpty(message = "Fill in this field")
    private String userToken;
}
