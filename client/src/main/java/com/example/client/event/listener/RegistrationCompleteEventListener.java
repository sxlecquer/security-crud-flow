package com.example.client.event.listener;

import com.example.client.entity.Student;
import com.example.client.event.RegistrationCompleteEvent;
import com.example.client.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Random;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
    @Autowired
    private StudentService studentService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        Student student = event.getStudent();
        Random random = new Random();
        String token = String.format(Locale.ROOT, "%06d", random.nextInt(1000000));
        studentService.saveVerificationToken(student, token);
//        String url = event.getApplicationUrl() + "/verifyEmail?token=" + token;
        log.info("Verification code to verify your email:\n{}", token);
    }
}
