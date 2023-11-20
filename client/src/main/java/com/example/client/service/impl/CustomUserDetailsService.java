package com.example.client.service.impl;

import com.example.client.entity.Student;
import com.example.client.entity.User;
import com.example.client.repository.CuratorRepository;
import com.example.client.repository.LecturerRepository;
import com.example.client.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

        if(user instanceof Student student) {
            Collection<? extends GrantedAuthority> authorities;
            if(student.isEnabled()) {
                authorities = getAuthorities(getAllRolesForUser(user.getRole()));
            } else {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER_NOT_VERIFIED"));
            }
            return new org.springframework.security.core.userdetails.User(
                    student.getEmail(),
                    student.getPassword(),
                    true,
                    true,
                    true,
                    true,
                    authorities
            );
        } else {
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    getAuthorities(getAllRolesForUser(user.getRole()))
            );
        }
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for(String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return authorities;
    }

    private List<String> getAllRolesForUser(String userRole) {
        List<String> result = new ArrayList<>();
        if(userRole.equalsIgnoreCase("USER_NOT_VERIFIED")) {
            result.add("USER_NOT_VERIFIED");
            return result;
        }
        List<String> roles = new ArrayList<>(List.of("USER", "MODERATOR", "ADMIN"));
        for(int i = 0; i < roles.size(); i++) {
            if(userRole.equalsIgnoreCase(roles.get(i))) {
                for(int j = 0; j <= i; j++) {
                    result.add(roles.get(j));
                }
                break;
            }
        }
        return result;
    }

    public void updateRole(String email, String role) {
        var user = loadUserByUsername(email);
        UsernamePasswordAuthenticationToken updatedAuthentication =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), getAuthorities(getAllRolesForUser(role)));
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }
}
