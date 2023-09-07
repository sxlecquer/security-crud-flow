package com.example.client.entity;

import jakarta.persistence.*;
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
    private String firstName;
    private String lastName;

    @Column(name = "phone")
    private String phoneNumber;

    @OneToOne(mappedBy = "parent")
    private Student student;
}
