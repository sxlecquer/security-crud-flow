package com.example.client.service.impl;

import com.example.client.entity.Curator;
import com.example.client.entity.Student;
import com.example.client.model.BasicInformationModel;
import com.example.client.repository.CuratorRepository;
import com.example.client.service.CuratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
public class CuratorServiceImpl implements CuratorService {
    private static final List<String> FIRST_NAMES = List.of("Linda", "Kimberly", "Laura", "Richard", "Mark", "Michael", "Margaret",
            "George", "Nancy", "Anthony", "William", "Sharon", "Jennifer", "Donna", "Barbara", "Patricia", "Joseph", "Daniel",
            "Kevin", "Karen", "Ronald", "Edward", "Elizabeth", "Thomas", "Michelle", "Alfred", "Deborah", "David", "James",
            "Sandra", "Mary", "Christopher", "Jessie", "Jeff", "Donald", "Shelly", "John", "Helen", "Bill", "Sarah", "Charles",
            "Ruth", "Betty", "Lisa", "Steven", "Paul", "Brian", "Susan", "Jason", "Carol");

    private static final List<String> LAST_NAMES = List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson",
            "Martin", "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
            "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green", "Adams", "Nelson",
            "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts");

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
                return curator;
            }
        }
        return null;
    }

    @Override
    public void fillCuratorTable() {
        Curator curator1 = new Curator();
        curator1.setFirstName("Shelly");
        curator1.setLastName("Smith");
        curator1.setEmail(curator1.getFirstName().toLowerCase(Locale.ROOT) + "." + curator1.getLastName().toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator1.setPassword(passwordEncoder.encode(curator1.getLastName() + "123"));

        Curator curator2 = new Curator();
        curator2.setFirstName("Donald");
        curator2.setLastName("Flores");
        curator2.setEmail(curator2.getFirstName().toLowerCase(Locale.ROOT) + "." + curator2.getLastName().toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator2.setPassword(passwordEncoder.encode(curator2.getLastName() + "123"));

        Curator curator3 = new Curator();
        curator3.setFirstName("Jennifer");
        curator3.setLastName("White");
        curator3.setEmail(curator3.getFirstName().toLowerCase(Locale.ROOT) + "." + curator3.getLastName().toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator3.setPassword(passwordEncoder.encode(curator3.getLastName() + "123"));

        Curator curator4 = new Curator();
        curator4.setFirstName("Sandra");
        curator4.setLastName("Scott");
        curator4.setEmail(curator4.getFirstName().toLowerCase(Locale.ROOT) + "." + curator4.getLastName().toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator4.setPassword(passwordEncoder.encode(curator4.getLastName() + "123"));

        curatorRepository.saveAll(List.of(curator1, curator2, curator3, curator4));
    }

    @Override
    public Curator findByEmail(String email) {
        return curatorRepository.findByEmail(email);
    }

    @Override
    public Curator findById(int id) {
        Optional<Curator> curator = curatorRepository.findById((long) id);
        return curator.orElse(null);
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        Curator curator = curatorRepository.findById((long) id).orElse(null);
        if(curator != null) {
            List<Student> students = curator.getStudents();
            Curator hired = hireCurator(students);
            students.forEach(student -> student.setCurator(hired));
            curator.setStudents(null);
            curatorRepository.save(hired);
            curatorRepository.deleteById(id);
        }
    }

    @Override
    public void changePassword(Curator curator, String newPassword) {
        curator.setPassword(passwordEncoder.encode(newPassword));
        curatorRepository.save(curator);
    }

    @Override
    public void saveCuratorChanges(Curator curator, BasicInformationModel basicInformationModel) {
        curator.setFirstName(basicInformationModel.getFirstName());
        curator.setLastName(basicInformationModel.getLastName());
        curatorRepository.save(curator);
    }

    private Curator hireCurator(List<Student> students) {
        Random random = new Random();
        String firstName = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
        String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
        Curator curator = new Curator();
        curator.setFirstName(firstName);
        curator.setLastName(lastName);
        curator.setEmail(firstName.toLowerCase(Locale.ROOT) + "." + lastName.toLowerCase(Locale.ROOT) + "@univ.cur.com");
        curator.setPassword(passwordEncoder.encode(lastName + "123"));
        curator.setStudents(students);
        return curator;
    }
}
