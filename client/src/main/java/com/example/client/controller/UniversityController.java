package com.example.client.controller;

import com.example.client.annotation.FieldsValueMatch;
import com.example.client.entity.*;
import com.example.client.event.EmailVerificationEvent;
import com.example.client.model.*;
import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import com.example.client.service.StudentService;
import com.example.client.service.UserService;
import com.example.client.service.impl.CustomUserDetailsService;
import com.example.client.token.TokenState;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@Slf4j
@ControllerAdvice
@SessionAttributes({"studentEmail", "verifyToken", "userEmail", "currentUser"})
public class UniversityController {
    // TODO:
    //  implement proper email sending :)
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

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private static final Map<Authentication, User> USER_MAP = new HashMap<>();

    @GetMapping("/")
    public String homePage() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        User user = getUser();
        if(user == null) user = initUser();
        model.addAttribute("username", user != null ? user.getFirstName() : null);
        return "home";
    }

    @GetMapping("/register")
    public String newUser(@ModelAttribute("student") StudentModel studentModel) {
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String register(@Valid @ModelAttribute("student") StudentModel studentModel, BindingResult bindingResult,
                           Model model,
                           @ModelAttribute("studentEmail") String email,
                           @ModelAttribute("verifyToken") String token,
                           RedirectAttributes redirectAttributes,
                           final HttpServletRequest request) {
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
        try {
            Student student = studentService.register(studentModel);
            request.login(studentModel.getEmail(), studentModel.getPassword());
            publisher.publishEvent(new EmailVerificationEvent(student));
            redirectAttributes.addFlashAttribute("studentEmail", student.getEmail());
            redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent(student).getToken());
            return "redirect:/register/verify-email";
        } catch (ServletException e) {
            log.error("ServletException was thrown during user registration: {}", e.getMessage());
        }
        return "register";
    }

    @GetMapping("/register/verify-email")
    public String verifyUser(@ModelAttribute("verificationToken") VerificationTokenModel verificationTokenModel,
                             @ModelAttribute("studentEmail") String email,
                             @ModelAttribute("verifyToken") String token,
                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyToken", token);
        return "verify_email";
    }

    @PostMapping("/register/verify-email")
    public String verifyEmail(@Valid @ModelAttribute("verificationToken") VerificationTokenModel verificationTokenModel,
                                    BindingResult bindingResult,
                                    @ModelAttribute("studentEmail") String email,
                                    @ModelAttribute("verifyToken") String token,
                                    RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyToken", token);
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
        userDetailsService.updateRole(email, "USER");
        return "success_verify_email";
    }

    @GetMapping("/register/resend-verification-code")
    public String resendVerificationCode(@RequestParam("email") String email,
                                         @RequestParam("token") String oldToken,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("studentEmail", email);
        redirectAttributes.addFlashAttribute("verifyToken", studentService.sendNewVerificationToken(oldToken));
        return "redirect:/register/verify-email";
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("emailModel") EmailModel emailModel, Model model,
                        @RequestParam(value = "email", required = false) String email,
                        @ModelAttribute("verifyToken") String token,
                        RedirectAttributes redirectAttributes) {
        model.addAttribute("email", email);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
        Object principal = authentication.getPrincipal();
        if(principal instanceof org.springframework.security.core.userdetails.User user) {
            if(user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER_NOT_VERIFIED"))) {
                redirectAttributes.addFlashAttribute("studentEmail", user.getUsername());
                redirectAttributes.addFlashAttribute("verifyToken", token);
                return "redirect:/register/verify-email";
            }
        }
        return "redirect:/home";
    }

    @GetMapping("/login/process")
    public String processLogin(RedirectAttributes redirectAttributes) {
        User user = initUser();
        if(user instanceof Student student && !student.isEnabled()) {
            publisher.publishEvent(new EmailVerificationEvent(student));
            redirectAttributes.addFlashAttribute("studentEmail", student.getEmail());
            redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent(student).getToken());
            return "redirect:/register/verify-email";
        }
        return "redirect:/home";
    }

    @GetMapping("/login/reset-password")
    public String resetPassword(@ModelAttribute("emailModel") EmailModel emailModel,
                                final HttpServletRequest httpServletRequest,
                                Model model) {
        User user;
        if((user = initUser()) != null) {
            emailModel.setEmail(user.getEmail());
            String token = generatePasswordTokenAndSendMail(user, httpServletRequest);
            model.addAttribute("passwordToken", token);
            model.addAttribute("linkSent", true);
        }
        return "reset_password";
    }

    @PostMapping("/login/reset-password")
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

    @GetMapping("/login/resend-password-token")
    public String resendPasswordToken(@ModelAttribute("emailModel") EmailModel emailModel,
                                      @RequestParam("token") String oldToken,
                                      final HttpServletRequest httpServletRequest,
                                      Model model,
                                      BindingResult bindingResult) {
        if(oldToken.isEmpty()) {
            FieldError fieldError = new FieldError("emailModel", "email", "Fill in this field");
            bindingResult.addError(fieldError);
            return "reset_password";
        }
        String newToken = userService.sendNewPasswordToken(oldToken);
        model.addAttribute("passwordToken", newToken);
        if(!newToken.isEmpty())
            sendResetPasswordMail(applicationUrl(httpServletRequest), newToken);
        model.addAttribute("linkSent", !newToken.isEmpty());
        return "reset_password";
    }

    @GetMapping("/login/save-password")
    public String savePassword(@RequestParam(value = "token", required = false) String token,
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
        return "redirect:/login/save";
    }

    @GetMapping("/login/save")
    public String save(@ModelAttribute("resetPasswordModel") ResetPasswordModel resetPasswordModel,
                        @ModelAttribute("userEmail") String email,
                        @ModelAttribute("currentUser") User user,
                        RedirectAttributes redirectAttributes) {
        if(user == null)
            return "redirect:/home";
        redirectAttributes.addFlashAttribute("userEmail", email);
        redirectAttributes.addFlashAttribute("currentUser", user);
        return "new_password";
    }

    @PatchMapping("/login/save-password")
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
        return "success_change_password";
    }

    @GetMapping("/change-password")
    public String changePassword(@ModelAttribute("changePasswordModel") ChangePasswordModel changePasswordModel) {
        if(getUser() == null) initUser();
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
        User user = getUser();
        if(userService.findUserByEmailAndPassword(user.getEmail(), changePasswordModel.getOldPassword()) == null) {
            FieldError fieldError = new FieldError("changePasswordModel", "oldPassword",
                    changePasswordModel.getOldPassword(), false, null, null, "Old password is incorrect");
            bindingResult.addError(fieldError);
            return "change_password";
        }
        userService.changePassword(user, changePasswordModel.getNewPassword());
        return "success_change_password";
    }

    @GetMapping("/profile")
    public String showProfile(@ModelAttribute("basicInformation") BasicInformationModel basicInformationModel) {
        User user = getUser();
        if(user == null) user = initUser();
        basicInformationModel.setFirstName(user.getFirstName());
        basicInformationModel.setLastName(user.getLastName());
        basicInformationModel.setEmail(user.getEmail());
        basicInformationModel.setRole(user.getRole());
        return "profile";
    }

    @PatchMapping("/profile")
    public String showProfile(@Valid @ModelAttribute("basicInformation") BasicInformationModel basicInformationModel,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        User user = getUser();
        basicInformationModel.setRole(user.getRole());
        if(bindingResult.hasErrors()) {
            return "profile";
        }
        Student tempStudent = new Student();
        tempStudent.setEmail(user.getEmail());
        userService.saveUserChanges(user, basicInformationModel);
        if(!tempStudent.getEmail().equals(user.getEmail())) {
            userDetailsService.updateRole(user.getEmail(), "USER_NOT_VERIFIED");
            publisher.publishEvent(new EmailVerificationEvent((Student) user));
            redirectAttributes.addFlashAttribute("studentEmail", user.getEmail());
            redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent((Student) user).getToken());
            return "redirect:/register/verify-email";
        }
        return "success_change_profile";
    }

    @GetMapping("/profile/parent-data")
    public String showParentData(@ModelAttribute("parentInformation") ParentInformationModel parentInformationModel) {
        User user = getUser();
        if(user == null) user = initUser();
        Student currentStudent = (Student) user;
        parentInformationModel.setParentFirst(currentStudent.getParent().getFirstName());
        parentInformationModel.setParentLast(currentStudent.getParent().getLastName());
        parentInformationModel.setParentMobile(currentStudent.getParent().getPhoneNumber());
        return "parent_data";
    }

    @PatchMapping("/profile/parent-data")
    public String showParentData(@Valid @ModelAttribute("parentInformation") ParentInformationModel parentInformationModel,
                                 BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "parent_data";
        }
        studentService.saveParentChanges((Student) getUser(), parentInformationModel);
        return "success_change_profile";
    }

    @GetMapping("/students")
    public String showAllStudents(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "students_list";
    }

    @GetMapping("/students/{id}")
    public String showStudent(@PathVariable("id") int id, Model model) {
        Student student = studentService.findById(id);
        model.addAttribute("student", student);
        return "student_info";
    }

    @DeleteMapping("/students/{id}")
    public String deleteStudent(@PathVariable("id") int id) {
        studentService.deleteById(id);
        return "redirect:/students";
    }

    @GetMapping("/curators")
    public String showAllCurators(Model model) {
        model.addAttribute("curators", curatorService.findAll());
        return "curators_list";
    }

    @GetMapping("/curators/{id}")
    public String showCurator(@PathVariable("id") int id, Model model) {
        Curator curator = curatorService.findById(id);
        model.addAttribute("curator", curator);
        return "curator_info";
    }

    @DeleteMapping("/curators/{id}")
    public String deleteCurator(@PathVariable("id") int id) {
        curatorService.deleteById(id);
        return "redirect:/curators";
    }

    @GetMapping("/lecturers")
    public String showAllLecturers(Model model) {
        model.addAttribute("lecturers", lecturerService.findAll());
        return "lecturers_list";
    }

    @GetMapping("/lecturers/{id}")
    public String showLecturer(@PathVariable("id") int id, Model model) {
        Lecturer lecturer = lecturerService.findById(id);
        model.addAttribute("lecturer", lecturer);
        return "lecturer_info";
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
        String url = applicationUrl + "/login/save-password?token=" + token;
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

    private User initUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User user) {
            String email = user.getAttribute("email") != null ? user.getAttribute("email")
                    : user.getAttribute("login") + "@github.com";
            USER_MAP.put(authentication, studentService.findByEmail(email));
        } else if(principal instanceof org.springframework.security.core.userdetails.User temp) {
            String email = temp.getUsername();
            User user = studentService.findByEmail(email);
            if (user == null) {
                user = curatorService.findByEmail(email);
                if (user == null) {
                    user = lecturerService.findByEmail(email);
                }
            }
            USER_MAP.put(authentication, user);
        }
        return USER_MAP.get(authentication);
    }

    private User getUser() {
        return USER_MAP.get(SecurityContextHolder.getContext().getAuthentication());
    }
}
