package com.example.client.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Lecturer extends User {
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
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role = "ADMIN";
}
