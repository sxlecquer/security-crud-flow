package com.example.client.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Curator {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "curator_seq"
    )
    @SequenceGenerator(
            name = "curator_seq",
            sequenceName = "CURATOR_SEQ",
            allocationSize = 1
    )
    private Long curatorId;

    @Size(min = 2, max = 15, message = "First name is too long or too short")
    @NotEmpty(message = "First name should not be empty")
    private String firstName;

    @Size(min = 2, max = 20, message = "Last name is too long or too short")
    @NotEmpty(message = "Last name should not be empty")
    private String lastName;

    @Email(message = "Email address is incorrect")
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 6, message = "Password is too simple")
    private String password;
    private String role = "MODERATOR";

    @OneToMany(mappedBy = "curator")
    private List<Student> students = new ArrayList<>();
}
