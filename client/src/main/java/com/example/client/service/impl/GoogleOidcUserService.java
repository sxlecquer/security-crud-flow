package com.example.client.service.impl;

import com.example.client.entity.GoogleOidcUser;
import com.example.client.entity.Student;
import com.example.client.model.StudentModel;
import com.example.client.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class GoogleOidcUserService extends OidcUserService {
    @Autowired
    private StudentService studentService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        GoogleOidcUser oidcUser = new GoogleOidcUser(super.loadUser(userRequest));
        String email = oidcUser.getAttribute("email");
        if(studentService.findByEmail(email) == null) {
            Student student = studentService.register(fillWithDummyData(oidcUser));
            if(oidcUser.getEmailVerified()) {
                student.setEnabled(true);
                studentService.save(student);
            }
        }
        return oidcUser;
    }

    private StudentModel fillWithDummyData(OidcUser oidcUser) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, Object> pair : oidcUser.getAttributes().entrySet()) {
            stringBuilder.append(pair.getKey())
                         .append(" - ")
                         .append(pair.getValue())
                         .append("\n");
        }
        log.debug("\nOAuth 2.0 token attributes received for Google user with id {}:\n{}", oidcUser.getSubject(), stringBuilder);
        String defaultVal = "google user";
        String firstName = oidcUser.getGivenName();
        String lastName = oidcUser.getFamilyName();
        String email = oidcUser.getAttribute("email");
        return new StudentModel(firstName, lastName, email, "Dummy123", "Dummy123",
                defaultVal, lastName, defaultVal);
    }
}
