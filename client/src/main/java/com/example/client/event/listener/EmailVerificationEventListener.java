package com.example.client.event.listener;

import com.example.client.entity.Student;
import com.example.client.event.EmailVerificationEvent;
import com.example.client.service.StudentService;
import com.example.client.service.impl.EmailSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Random;

@Component
@Slf4j
public class EmailVerificationEventListener implements ApplicationListener<EmailVerificationEvent> {
    @Autowired
    private StudentService studentService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(EmailVerificationEvent event) {
        Student student = event.getStudent();
        Random random = new Random();
        String token = String.format(Locale.ROOT, "%06d", random.nextInt(1000000));
        studentService.saveVerificationToken(student, token);
        emailSenderService.sendEmail(student.getEmail(), "Verification code", "Verification code to verify your email:\n" + token);
        log.info("Verification code to verify your email:\n{}", token);
    }
}
