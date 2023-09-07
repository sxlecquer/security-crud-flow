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
public class Student {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "student_seq"
    )
    @SequenceGenerator(
            name = "student_seq",
            sequenceName = "STUDENT_SEQ",
            allocationSize = 1
    )
    private Long studentId;

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
    private String role = "USER";
    private boolean enabled = false;

    @OneToOne(
            cascade = CascadeType.ALL,
            optional = false
    )
    @JoinColumn(
            name = "parent_id",
            referencedColumnName = "parentId"
    )
    private Parent parent;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "curator_id",
            referencedColumnName = "curatorId"
    )
    private Curator curator;

    @ManyToMany
    @JoinTable(
            name = "student_lecturer",
            joinColumns = @JoinColumn(
                    name = "student_id",
                    referencedColumnName = "studentId"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "lecturer_id",
                    referencedColumnName = "lecturerId"
            )
    )
    private List<Lecturer> lecturers = new ArrayList<>();

    @OneToOne(
            mappedBy = "student",
            cascade = CascadeType.ALL
    )
    private VerificationToken verificationToken;
}
