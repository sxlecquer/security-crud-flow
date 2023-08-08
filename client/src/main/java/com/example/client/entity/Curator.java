package com.example.client.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

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

    @Size(min = 2, max = 15, message = "first name is too long or too short!")
    @NotEmpty(message = "first name should not be empty!")
    private String firstName;

    @Size(min = 2, max = 20, message = "last name is too long or too short!")
    @NotEmpty(message = "last name should not be empty!")
    private String lastName;

    @Email(message = "email address is incorrect!")
    @NotEmpty(message = "email should not be empty!")
    private String email;
    private String password;
    private String role = "MODERATOR";

    @OneToMany(mappedBy = "curator")
    private Set<Student> student;
}
