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

import java.util.Objects;
import java.util.UUID;

@Controller
@Slf4j
@ControllerAdvice
@SessionAttributes({"studentEmail", "verifyToken", "userEmail", "currentUser"})
public class UniversityController {
    // TODO:
    //  fix mappings to work correct(make currentUser initialized when app restarts),
    //  create mappings for each entity role,
    //  set up web security,
    //  delete tokens from db when app starts ???
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

    private User currentUser;

    @GetMapping("/home")
    public String homePage(Model model) {
//        lecturerService.fillLecturerTable();
//        curatorService.fillCuratorTable();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof OAuth2User user) {
            if (user.getAttribute("given_name") != null) {
                model.addAttribute("username", user.getAttribute("given_name"));
            } else if (user.getAttribute("name") != null) {
                model.addAttribute("username", user.getAttribute("name"));
            } else {
                model.addAttribute("username", user.getAttribute("login"));
            }
            String email = user.getAttribute("email") != null ? user.getAttribute("email")
                    : user.getAttribute("login") + "@github.com";
            currentUser = studentService.findByEmail(email);
        } else if(principal instanceof org.springframework.security.core.userdetails.User temp) {
            String email = temp.getUsername();
            currentUser = studentService.findByEmail(email);
            if (currentUser == null) {
                currentUser = curatorService.findByEmail(email);
                if (currentUser == null) {
                    currentUser = lecturerService.findByEmail(email);
                }
            }
            model.addAttribute("username", currentUser.getFirstName());
        }
        return "home";
    }

    @GetMapping("/login/process")
    public String processLogin(RedirectAttributes redirectAttributes) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof OAuth2User user) {
            String email = user.getAttribute("email") != null ? user.getAttribute("email")
                    : user.getAttribute("login") + "@github.com";
            currentUser = studentService.findByEmail(email);
        } else if(principal instanceof org.springframework.security.core.userdetails.User temp) {
            String email = temp.getUsername();
            currentUser = studentService.findByEmail(email);
            if (currentUser == null) {
                currentUser = curatorService.findByEmail(email);
                if (currentUser == null) {
                    currentUser = lecturerService.findByEmail(email);
                }
            } else {
                Student student = (Student) currentUser;
                if(!student.isEnabled()) {
                    publisher.publishEvent(new EmailVerificationEvent(student));
                    redirectAttributes.addFlashAttribute("studentEmail", student.getEmail());
                    redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent(student).getToken());
                    return "redirect:/register/verify-email";
                }
            }
        }
        return "redirect:/home";
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
//        studentService.deleteById(66);
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

    @GetMapping("/login/reset-password")
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
        if(currentUser == null)
            return "redirect:/home";
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
        if(!tempStudent.getEmail().equals(currentUser.getEmail())) {
            userDetailsService.updateRole(currentUser.getEmail(), "USER_NOT_VERIFIED");
            publisher.publishEvent(new EmailVerificationEvent((Student) currentUser));
            redirectAttributes.addFlashAttribute("studentEmail", currentUser.getEmail());
            redirectAttributes.addFlashAttribute("verifyToken", studentService.findVerificationTokenByStudent((Student) currentUser).getToken());
            return "redirect:/register/verify-email";
        }
        return "success_change_profile";
    }

    @GetMapping("/profile/parent-data")
    public String showParentData(@ModelAttribute("parentInformation") ParentInformationModel parentInformationModel) {
        Student currentStudent = (Student) currentUser;
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
}
