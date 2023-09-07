package com.example.client.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Entity
@Data
public class Lecturer {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "lecturer_seq"
    )
    @SequenceGenerator(
            name = "lecturer_seq",
            sequenceName = "LECTURER_SEQ",
            allocationSize = 1
    )
    private Long lecturerId;

    @Size(min = 2, max = 15, message = "first name is too long or too short!")
    @NotEmpty(message = "first name should not be empty!")
    private String firstName;

    @Size(min = 2, max = 20, message = "last name is too long or too short!")
    @NotEmpty(message = "last name should not be empty!")
    private String lastName;

    @Email(message = "email address is incorrect!")
    @NotEmpty(message = "email should not be empty!")
    private String email;

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 6, message = "Password is too simple")
    private String password;
    private String role = "ADMIN";
}
