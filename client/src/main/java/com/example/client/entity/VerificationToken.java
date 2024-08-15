package com.example.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Entity
@NoArgsConstructor
@Data
public class VerificationToken {
    private static final int EXPIRATION_TIME = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;
    private String token;
    private Date expirationTime;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "student_id",
            referencedColumnName = "studentId",
            nullable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    public VerificationToken(Student student, String token) {
        this.student = student;
        this.token = token;
        this.expirationTime = calculateExpirationTime();
    }

    public Date calculateExpirationTime() {
        Calendar calendar = Calendar.getInstance(Locale.ROOT);
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME);
        return calendar.getTime();
    }
}
