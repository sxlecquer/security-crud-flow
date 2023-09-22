package com.example.client.event;

import com.example.client.entity.Student;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class EmailVerificationEvent extends ApplicationEvent {
    private Student student;
    public EmailVerificationEvent(Student student) {
        super(student);
        this.student = student;
    }
}
