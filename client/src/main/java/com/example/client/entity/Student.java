package com.example.client.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Student extends User {
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
    private String firstName;
    private String lastName;
    @Getter(AccessLevel.NONE)
    private String email;
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

    @Override
    public String getEmail() {
        return email;
    }
}
