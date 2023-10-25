package com.example.client.controller;

import com.example.client.annotation.FieldsValueMatch;
import com.example.client.entity.*;
import com.example.client.event.EmailVerificationEvent;
import com.example.client.model.*;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import com.example.client.service.UserService;
import com.example.client.token.TokenState;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.UUID;

@Controller
@Slf4j
@ControllerAdvice
@SessionAttributes({"studentEmail", "verifyToken", "userEmail", "currentUser"})
public class UniversityController {
    // TODO:
    //  fix in profile:
    //      add profile ref on home page,
    //      implement proper email sending :)
    //  realize:
    //      mappings for each entity role,
    //      auth-server and login using OAuth2.0....

    @Autowired
    private UserService userService;

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

    private User currentUser;

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
        publisher.publishEvent(new EmailVerificationEvent(student));
        redirectAttributes.addFlashAttribute("studentEmail", student.getEmail());
        redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent(student).getToken());
        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email")
    public String verifyUser(@ModelAttribute("verificationToken") VerificationTokenModel verificationTokenModel,
                             @ModelAttribute("studentEmail") String email,
                             @ModelAttribute("verifyToken") String token,
                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyEmail", token);
        return "verify_email";
    }

    @PostMapping("/verify-email")
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
        TokenState response = studentService.validateVerificationToken(verificationTokenModel.getUserToken());
        if(response == TokenState.WRONG) {
            fieldError = new FieldError("verificationTokenModel", "userToken", verificationTokenModel.getUserToken(),
                    false, null, null, "Verification code is incorrect");
            bindingResult.addError(fieldError);
            return "verify_email";
        } else if(response == TokenState.EXPIRED) {
            fieldError = new FieldError("verificationTokenModel", "userToken", verificationTokenModel.getUserToken(),
                    false, null, null, "Verification code is expired. Request a new one using the link above");
            bindingResult.addError(fieldError);
            return "verify_email";
        }
        return "success_verify_email";
    }

    @GetMapping("/resend-verification-code")
    public String resendVerificationCode(@RequestParam("email") String email,
                                         @RequestParam("token") String oldToken,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyToken", studentService.sendNewVerificationToken(oldToken));
        return "redirect:/verify-email";
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
        if(!checkIfCredentialsCorrect(userModel.getEmail(), userModel.getPassword())) {
            FieldError fieldError = new FieldError("userModel", "password", userModel.getPassword(),
                    false, null, null, "Incorrect email address or password");
            bindingResult.addError(fieldError);
            return "login";
        }
        currentUser = userService.findUserByEmailAndPassword(userModel.getEmail(), userModel.getPassword());
        return "redirect:/home";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@ModelAttribute("emailModel") EmailModel emailModel,
                                final HttpServletRequest httpServletRequest,
                                Model model) {
        if(currentUser != null) {
            emailModel.setEmail(currentUser.getEmail());
            String token = generatePasswordTokenAndSendMail(currentUser, httpServletRequest);
            model.addAttribute("passwordToken", token);
            model.addAttribute("linkSent", true);
        }
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("emailModel") EmailModel emailModel,
                                BindingResult bindingResult,
                                final HttpServletRequest httpServletRequest,
                                Model model) {
        if(bindingResult.hasErrors()) {
            return "reset_password";
        }
        String email = emailModel.getEmail();
        User user = studentService.findByEmail(email);
        if(user == null) {
            user = curatorService.findByEmail(email);
            if (user == null) {
                user = lecturerService.findByEmail(email);
            }
        }
        if(user != null) {
            String token = generatePasswordTokenAndSendMail(user, httpServletRequest);
            model.addAttribute("passwordToken", token);
            model.addAttribute("linkSent", true);
        } else {
            FieldError fieldError = new FieldError("passwordModel", "email", emailModel.getEmail(),
                    false, null, null, "User with such email doesn't exist");
            bindingResult.addError(fieldError);
            return "reset_password";
        }
        return "reset_password";
    }

    @GetMapping("/resend-password-token")
    public String resendPasswordToken(@ModelAttribute("emailModel") EmailModel emailModel,
                                      @RequestParam("token") String oldToken,
                                      final HttpServletRequest httpServletRequest,
                                      Model model) {
        String newToken = userService.sendNewPasswordToken(oldToken);
        model.addAttribute("passwordToken", newToken);
        if(!newToken.isEmpty())
            sendResetPasswordMail(applicationUrl(httpServletRequest), newToken);
        model.addAttribute("linkSent", !newToken.isEmpty());
        return "reset_password";
    }

    @GetMapping("/save-password")
    public String savePassword(@RequestParam("token") String token,
                               RedirectAttributes redirectAttributes) {
        TokenState response = userService.validatePasswordToken(token);
        if(response == TokenState.WRONG) {
            return "password_token/wrong";
        } else if(response == TokenState.EXPIRED) {
            return "password_token/expired";
        }
        User user = userService.findUserByPasswordToken(userService.findPasswordToken(token));
        redirectAttributes.addFlashAttribute("userEmail",
                user.getEmail());
        redirectAttributes.addFlashAttribute("currentUser", user);
        return "redirect:/save";
    }

    @GetMapping("/save")
    public String save(@ModelAttribute("resetPasswordModel") ResetPasswordModel resetPasswordModel,
                        @ModelAttribute("userEmail") String email,
                        @ModelAttribute("currentUser") User user,
                        RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("userEmail", email);
        redirectAttributes.addFlashAttribute("currentUser", user);
        return "new_password";
    }

    @PatchMapping("/save-password")
    public String savePassword(@Valid @ModelAttribute("resetPasswordModel") ResetPasswordModel resetPasswordModel,
                               BindingResult bindingResult,
                               @ModelAttribute("userEmail") String email,
                               @ModelAttribute("currentUser") User user,
                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("userEmail", email);
        redirectAttributes.addFlashAttribute("currentUser", user);
        if(!Objects.equals(resetPasswordModel.getNewPassword(), resetPasswordModel.getConfirmNewPassword())) {
            Class<? extends ResetPasswordModel> clazz = resetPasswordModel.getClass();
            FieldsValueMatch fieldsValueMatch = clazz.getAnnotation(FieldsValueMatch.class);
            FieldError fieldError = new FieldError("resetPasswordModel", "confirmNewPassword", fieldsValueMatch.message());
            bindingResult.addError(fieldError);
            return "new_password";
        }
        if(bindingResult.hasErrors()) {
            return "new_password";
        }
        userService.changePassword(user, resetPasswordModel.getNewPassword());
        currentUser = user;
        return "success_change_password";
    }

    @GetMapping("/change-password")
    public String changePassword(@ModelAttribute("changePasswordModel") ChangePasswordModel changePasswordModel) {
        return "change_password";
    }

    @PatchMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordModel") ChangePasswordModel changePasswordModel,
                                 BindingResult bindingResult) {
        if(!Objects.equals(changePasswordModel.getNewPassword(), changePasswordModel.getConfirmNewPassword())) {
            Class<? extends ChangePasswordModel> clazz = changePasswordModel.getClass();
            FieldsValueMatch fieldsValueMatch = clazz.getAnnotation(FieldsValueMatch.class);
            FieldError fieldError = new FieldError("changePasswordModel", "confirmNewPassword", fieldsValueMatch.message());
            bindingResult.addError(fieldError);
            return "change_password";
        }
        if(bindingResult.hasErrors()) {
            return "change_password";
        }
        if(userService.findUserByEmailAndPassword(currentUser.getEmail(), changePasswordModel.getOldPassword()) == null) {
            FieldError fieldError = new FieldError("changePasswordModel", "oldPassword",
                    changePasswordModel.getOldPassword(), false, null, null, "Old password is incorrect");
            bindingResult.addError(fieldError);
            return "change_password";
        }
        userService.changePassword(currentUser, changePasswordModel.getNewPassword());
        return "success_change_password";
    }

    @GetMapping("/profile")
    public String showProfile(@ModelAttribute("basicInformation") BasicInformationModel basicInformationModel) {
        basicInformationModel.setFirstName(currentUser.getFirstName());
        basicInformationModel.setLastName(currentUser.getLastName());
        basicInformationModel.setEmail(currentUser.getEmail());
        basicInformationModel.setRole(currentUser.getRole());
        return "profile";
    }

    @PatchMapping("/profile")
    public String showProfile(@Valid @ModelAttribute("basicInformation") BasicInformationModel basicInformationModel,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        basicInformationModel.setRole(currentUser.getRole());
        if(bindingResult.hasErrors()) {
            return "profile";
        }
        Student tempStudent = new Student();
        tempStudent.setEmail(currentUser.getEmail());
        userService.saveUserChanges(currentUser, basicInformationModel);
        if(!tempStudent.getEmail().equals(basicInformationModel.getEmail())) {
            publisher.publishEvent(new EmailVerificationEvent((Student) currentUser));
            redirectAttributes.addFlashAttribute("studentEmail", currentUser.getEmail());
            redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent((Student) currentUser).getToken());
            return "redirect:/verify-email";
        }
        return "success_change_profile";
    }

    @GetMapping("/parent-data")
    public String showParentData(@ModelAttribute("parentInformation") ParentInformationModel parentInformationModel) {
        Student currentStudent = (Student) currentUser;
        parentInformationModel.setParentFirst(currentStudent.getParent().getFirstName());
        parentInformationModel.setParentLast(currentStudent.getParent().getLastName());
        parentInformationModel.setParentMobile(currentStudent.getParent().getPhoneNumber());
        return "parent_data";
    }

    @PatchMapping("/parent-data")
    public String showParentData(@Valid @ModelAttribute("parentInformation") ParentInformationModel parentInformationModel,
                                 BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "parent_data";
        }
        studentService.saveParentChanges((Student) currentUser, parentInformationModel);
        return "success_change_profile";
    }

    // ---------------Model attributes---------------
    @ModelAttribute("needForStaff")
    public boolean isNeedForStaff() {
        return studentService.findAll().size() >= curatorService.STUDENT_LIMIT * curatorService.findAll().size();
    }

    // ---------------Session attributes---------------
    @ModelAttribute("studentEmail")
    public String assignStudentCurrentEmail() {
        return "";
    }

    @ModelAttribute("verifyToken")
    public String assignVerificationToken() {
        return "";
    }

    @ModelAttribute("userEmail")
    public String assignUserCurrentEmail() {
        return "";
    }

    @ModelAttribute("currentUser")
    public User assignCurrentUser() {
        return null;
    }

    // ---------------Private methods---------------
    private void sendResetPasswordMail(String applicationUrl, String token) {
        String url = applicationUrl + "/save-password?token=" + token;
        log.info("Click the link to reset your password: {}", url);
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

    private boolean checkIfCredentialsCorrect(String email, String password) {
        return isUserExists(email, password);
    }

    private String applicationUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":"
                + request.getServerPort() + request.getContextPath();
    }

    private String generatePasswordTokenAndSendMail(User user, HttpServletRequest request) {
        String token = UUID.randomUUID().toString();
        userService.savePasswordToken(user, token);
        sendResetPasswordMail(applicationUrl(request), token);
        return token;
    }
}
