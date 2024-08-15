package com.example.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Entity
@Data
@NoArgsConstructor
public class PasswordToken {
    private static final int EXPIRATION_TIME = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;
    private String token;
    private Date expirationTime;

    @JoinColumn(
            name = "user_and_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_USER_PASSWORD_TOKEN")
    )
    private String userAndId;

    public PasswordToken(User user, String token) {
        if(user instanceof Student) {
            userAndId = Student.class.getSimpleName() + " " + ((Student) user).getStudentId();
        } else if(user instanceof Curator) {
            userAndId = Curator.class.getSimpleName() + " " + ((Curator) user).getCuratorId();
        } else if(user instanceof Lecturer) {
            userAndId = Lecturer.class.getSimpleName() + " " + ((Lecturer) user).getLecturerId();
        }
        this.token = token;
        this.expirationTime = calculateExpirationTime();
    }

    public Date calculateExpirationTime() {
        Calendar calendar = Calendar.getInstance(Locale.ROOT);
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME);
        return calendar.getTime();
    }
}
