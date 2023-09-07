package com.example.client.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationTokenModel {
    @NotEmpty(message = "Verification code should not be empty")
    private String userToken;
}
