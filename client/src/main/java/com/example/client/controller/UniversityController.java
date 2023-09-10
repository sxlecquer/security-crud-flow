package com.example.client.controller;

import com.example.client.annotation.FieldsValueMatch;
import com.example.client.entity.Student;
import com.example.client.event.RegistrationCompleteEvent;
import com.example.client.model.StudentModel;
import com.example.client.model.UserModel;
import com.example.client.model.VerificationTokenModel;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@Slf4j
@ControllerAdvice
@SessionAttributes({"studentEmail", "verifyToken"})
public class UniversityController {
    // TODO:
    //  fix login functionality(password check in db),
    //  fix login view(forgot password),
    //  realize "forgot password" func

    @Autowired
    private StudentService studentService;

    @Autowired
    private CuratorService curatorService;

    @Autowired
    private LecturerService lecturerService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String homePage() {
//        lecturerService.fillLecturerTable();
//        curatorService.fillCuratorTable();
//        studentService.deleteById(11);
        return "home";
    }

    @GetMapping("/register")
    public String newUser(@ModelAttribute("student") StudentModel studentModel) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("student") StudentModel studentModel, BindingResult bindingResult,
                           Model model,
                           @ModelAttribute("studentEmail") String email,
                           @ModelAttribute("verifyToken") String token,
                           RedirectAttributes redirectAttributes) {
        if(!Objects.equals(studentModel.getPassword(), studentModel.getConfirmPassword())) {
            Class<? extends StudentModel> clazz = studentModel.getClass();
            FieldsValueMatch fieldsValueMatch = clazz.getAnnotation(FieldsValueMatch.class);
            FieldError fieldError = new FieldError("studentModel", "confirmPassword", fieldsValueMatch.message());
            bindingResult.addError(fieldError);
        }
        if(bindingResult.hasErrors()) {
            return "register";
        }
        if(isUserExists(studentModel.getEmail(), studentModel.getPassword())) {
            model.addAttribute("userExists", true);
            return "register";
        }
        Student student = studentService.register(studentModel);
        publisher.publishEvent(new RegistrationCompleteEvent(student));
        redirectAttributes.addFlashAttribute("studentEmail", student.getEmail());
        redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent(student).getToken());
        return "redirect:/verifyEmail";
    }

    @GetMapping("/verifyEmail")
    public String verifyUser(@ModelAttribute("verificationToken") VerificationTokenModel verificationTokenModel,
                             @ModelAttribute("studentEmail") String email,
                             @ModelAttribute("verifyToken") String token,
                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyEmail", token);
        return "verify_email";
    }

    @PostMapping("/verifyEmail")
    public String verifyEmail(@Valid @ModelAttribute("verificationToken") VerificationTokenModel verificationTokenModel,
                                    BindingResult bindingResult,
                                    @ModelAttribute("studentEmail") String email,
                                    @ModelAttribute("verifyToken") String token,
                                    RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyEmail", token);
        if(bindingResult.hasErrors())
            return "verify_email";
        FieldError fieldError;
        String response = studentService.validateVerificationToken(verificationTokenModel.getUserToken());
        if(response.equalsIgnoreCase("wrong")) {
            fieldError = new FieldError("verificationTokenModel",
                    "userToken", "Verification code is incorrect");
            bindingResult.addError(fieldError);
            return "verify_email";
        } else if(response.equalsIgnoreCase("expired")) {
            fieldError = new FieldError("verificationTokenModel",
                    "userToken", "Verification code is expired.\n\nRequest a new one using the link above");
            bindingResult.addError(fieldError);
            return "verify_email";
        }
        return "success_verify_email";
    }

    @GetMapping("/resendVerificationCode")
    public String resendVerificationCode(@ModelAttribute("verificationToken") VerificationTokenModel verificationToken,
                                         @RequestParam("email") String email,
                                         @RequestParam("token") String oldToken,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyToken", studentService.sendNewVerificationToken(oldToken));
        return "redirect:/verifyEmail";
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("user") UserModel userModel, Model model,
                        @RequestParam(value = "email", required = false) String email) {
        model.addAttribute("email", email);
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("user") UserModel userModel, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "login";
        }
        if(!(studentService.checkIfCredentialsCorrect(userModel.getEmail(), userModel.getPassword())
                || curatorService.checkIfCredentialsCorrect(userModel.getEmail(), userModel.getPassword())
                || lecturerService.checkIfCredentialsCorrect(userModel.getEmail(), userModel.getPassword()))) {
            FieldError fieldError = new FieldError("userModel", "password", "Incorrect email address or password");
            bindingResult.addError(fieldError);
            return "login";
        }
        return "redirect:/home";
    }

    @ModelAttribute("needForStaff")
    public boolean isNeedForStaff() {
        return studentService.findAll().size() >= curatorService.STUDENT_LIMIT * curatorService.findAll().size();
    }

    @ModelAttribute("studentEmail")
    public String assignStudentCurrentEmail() {
        return "";
    }

    @ModelAttribute("verifyToken")
    public String assignVerificationToken() {
        return "";
    }

    private boolean isUserExists(String email, String password) {
        String curatorPassword = curatorService.findPasswordByEmail(email);
        if(curatorPassword != null && passwordEncoder.matches(password, curatorPassword))
            return true;
        String lecturerPassword = lecturerService.findPasswordByEmail(email);
        if(lecturerPassword != null && passwordEncoder.matches(password, lecturerPassword))
            return true;
        String studentPassword = studentService.findPasswordByEmail(email);
        return studentPassword != null && passwordEncoder.matches(password, studentPassword);
    }
}
