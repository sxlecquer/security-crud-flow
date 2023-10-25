package com.example.client.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Curator extends User {
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
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role = "MODERATOR";

    @OneToMany(mappedBy = "curator")
    private List<Student> students = new ArrayList<>();
}
