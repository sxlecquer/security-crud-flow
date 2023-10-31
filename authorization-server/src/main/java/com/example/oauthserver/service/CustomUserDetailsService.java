package com.example.oauthserver.service;

import com.example.client.entity.Student;
import com.example.client.entity.User;
import com.example.client.repository.CuratorRepository;
import com.example.client.repository.LecturerRepository;
import com.example.client.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CuratorRepository curatorRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = studentRepository.findByEmail(email);

        if(user == null) {
            user = curatorRepository.findByEmail(email);
            if (user == null) {
                user = lecturerRepository.findByEmail(email);
                if(user == null) {
                    throw new UsernameNotFoundException("User Not Found");
                }
            }
        }

        if(!(user instanceof Student)) {
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    getAuthorities(List.of(user.getRole()))
            );
        } else {
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    ((Student) user).isEnabled(),
                    true,
                    true,
                    true,
                    getAuthorities(List.of(user.getRole()))
            );
        }
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for(String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }
}
