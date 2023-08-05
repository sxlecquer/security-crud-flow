package com.example.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
