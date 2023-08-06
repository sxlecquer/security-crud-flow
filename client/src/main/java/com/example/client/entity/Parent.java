package com.example.client.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Parent {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "parent_seq"
    )
    @SequenceGenerator(
            name = "parent_seq",
            sequenceName = "PARENT_SEQ",
            allocationSize = 1
    )
    private Long parentId;

    @Size(min = 2, max = 15, message = "first name is too long or too short!")
    @NotEmpty(message = "first name should not be empty!")
    private String firstName;

    @Size(min = 2, max = 20, message = "last name is too long or too short!")
    @NotEmpty(message = "last name should not be empty!")
    private String lastName;
    private String phoneNumber;
    private String role = "GUEST";

    @OneToOne(mappedBy = "parent")
    private Student student;
}
