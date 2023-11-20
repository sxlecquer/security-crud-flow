package com.example.client.service.impl;

import com.example.client.entity.GitHubOAuth2User;
import com.example.client.entity.Student;
import com.example.client.model.StudentModel;
import com.example.client.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GitHubOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private StudentService studentService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        GitHubOAuth2User oAuth2User = new GitHubOAuth2User(getAuthorities(), super.loadUser(userRequest).getAttributes(), "id");
        String email = oAuth2User.getAttribute("email") != null ?
                oAuth2User.getAttribute("email") : oAuth2User.getAttribute("login") + "@github.com";
        if(studentService.findByEmail(email) == null) {
            Student student = studentService.register(fillWithDummyData(oAuth2User));
            String mail = oAuth2User.getAttribute("email");
            if(mail != null && mail.equals(student.getEmail())) {
                student.setEnabled(true);
                studentService.save(student);
            }
        }
        return oAuth2User;
    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private StudentModel fillWithDummyData(OAuth2User oAuth2User) {
        Map<String, Object> userData = oAuth2User.getAttributes();
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, Object> pair : userData.entrySet()) {
            stringBuilder.append(pair.getKey())
                    .append(" - ")
                    .append(pair.getValue())
                    .append("\n");
        }
        log.debug("\nOAuth 2.0 token attributes received for GitHub user with id {}:\n{}", userData.get("id"), stringBuilder);
        String firstName = userData.getOrDefault("name", userData.get("login")).toString();
        String lastName = userData.getOrDefault("html_url", "github user").toString();
        String[] username = firstName.split(" ", 2);
        if(username.length > 1) {
            firstName = username[0];
            lastName = username[1];
        }
        String email = userData.get("email") != null ? oAuth2User.getAttribute("email") : oAuth2User.getAttribute("login") + "@github.com";
        return new StudentModel(firstName, lastName, email, "Dummy123", "Dummy123",
                "github user", lastName, "github user");
    }
}
