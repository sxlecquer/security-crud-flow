package com.example.client.service.impl;

import com.example.client.entity.Curator;
import com.example.client.entity.Student;
import com.example.client.repository.CuratorRepository;
import com.example.client.service.CuratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CuratorServiceImpl implements CuratorService {
    /*private static final List<String> FIRST_NAMES = List.of("Linda", "Kimberly", "Laura", "Richard", "Mark", "Michael", "Margaret",
            "George", "Nancy", "Anthony", "William", "Sharon", "Jennifer", "Donna", "Barbara", "Patricia", "Joseph", "Daniel",
            "Kevin", "Karen", "Ronald", "Edward", "Elizabeth", "Thomas", "Michelle", "Alfred", "Deborah", "David", "James",
            "Sandra", "Mary", "Christopher", "Jessie", "Jeff", "Donald", "Shelly", "John", "Helen", "Bill", "Sarah", "Charles",
            "Ruth", "Betty", "Lisa", "Steven", "Paul", "Brian", "Susan", "Jason", "Carol");

    private static final List<String> LAST_NAMES = List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson",
            "Martin", "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
            "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green", "Adams", "Nelson",
            "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts");*/

    @Autowired
    private CuratorRepository curatorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String findPasswordByEmail(String email) {
        return curatorRepository.findPasswordByEmail(email);
    }

    @Override
    public List<Curator> findAll() {
        return curatorRepository.findAll();
    }

    @Override
    public Curator assignCurator(Student student) {
        for(Curator curator : findAll()) {
            if(curator.getStudents().size() < STUDENT_LIMIT) {
                curator.getStudents().add(student);
//                curatorRepository.save(curator);
                return curator;
            }
        }
        return null;
    }

    @Override
    public void fillCuratorTable() {
        /*Curator curator1 = new Curator();
        curator1.setFirstName("Shelly");
        curator1.setLastName("Smith");
        curator1.setEmail(curator1.getFirstName().toLowerCase(Locale.ROOT) + "." + curator1.getLastName().toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator1.setPassword(passwordEncoder.encode("Smith123"));
        curatorRepository.save(curator1);

        Curator curator2 = new Curator();
        curator2.setFirstName("Donald");
        curator2.setLastName("Flores");
        curator2.setEmail(curator2.getFirstName().toLowerCase(Locale.ROOT) + "." + curator2.getLastName().toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator2.setPassword(passwordEncoder.encode("Flores123"));
        curatorRepository.save(curator2);*/
    }
}
