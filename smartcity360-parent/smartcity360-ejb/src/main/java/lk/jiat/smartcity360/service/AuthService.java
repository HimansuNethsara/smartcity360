package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Role;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.dto.ChangePasswordRequest;
import lk.jiat.smartcity360.dto.ProfileRequest;
import lk.jiat.smartcity360.dto.RegisterRequest;
import lk.jiat.smartcity360.dto.SessionView;
import lk.jiat.smartcity360.repository.RoleRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.security.PasswordService;
import lk.jiat.smartcity360.security.RoleNames;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.util.Comparator;
import java.util.regex.Pattern;

@Stateless
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern PHONE = Pattern.compile("[0-9+() -]{7,20}");

    @EJB
    private UserRepository userRepository;
    @EJB
    private RoleRepository roleRepository;
    @EJB
    private PasswordService passwords;

    public SessionView forLogin(String username) {
        User u = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new BusinessRuleException("Invalid username or password."));
        if (!Boolean.TRUE.equals(u.getEnabled())) {
            throw new BusinessRuleException("This account is disabled.");
        }
        return view(u);
    }

    public SessionView me(Long userId) {
        User u = userRepository.findByIdWithRoles(userId);
        if (u == null) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }
        return view(u);
    }

    public SessionView register(RegisterRequest req) {
        String username = clean(req.getUsername());
        String email = clean(req.getEmail()).toLowerCase();
        String fullName = clean(req.getFullName());
        String phone = clean(req.getPhone());
        if (!USERNAME.matcher(username).matches()) {
            throw new BusinessRuleException("Username must be 3-30 characters: letters, digits, dot, dash or underscore.");
        }
        checkEmail(email);
        checkName(fullName);
        checkPhone(phone);
        passwords.requireStrong(req.getPassword());
        if (userRepository.usernameTaken(username)) {
            throw new BusinessRuleException("That username is already taken.");
        }
        if (userRepository.emailTaken(email)) {
            throw new BusinessRuleException("That email is already registered.");
        }
        Role citizen = roleRepository.findByName(RoleNames.CITIZEN)
                .orElseThrow(() -> new IllegalStateException("Role CITIZEN is missing from the roles table"));
        User u = User.builder()
                .username(username)
                .password(passwords.hash(req.getPassword()))
                .email(email)
                .fullName(fullName)
                .phone(phone.isEmpty() ? null : phone)
                .build();
        u.getRoles().add(citizen);
        userRepository.save(u);
        return view(u);
    }

    public SessionView updateProfile(Long userId, ProfileRequest req) {
        User u = userRepository.findByIdWithRoles(userId);
        if (u == null) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }
        String email = clean(req.getEmail()).toLowerCase();
        String fullName = clean(req.getFullName());
        String phone = clean(req.getPhone());
        checkEmail(email);
        checkName(fullName);
        checkPhone(phone);
        User other = userRepository.findByEmail(email).orElse(null);
        if (other != null && !other.getId().equals(userId)) {
            throw new BusinessRuleException("That email is already registered.");
        }
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPhone(phone.isEmpty() ? null : phone);
        return view(u);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User u = userRepository.findById(userId);
        if (u == null) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }
        if (!passwords.matches(req.getCurrentPassword(), u.getPassword())) {
            throw new BusinessRuleException("Current password is not correct.");
        }
        passwords.requireStrong(req.getNewPassword());
        if (passwords.matches(req.getNewPassword(), u.getPassword())) {
            throw new BusinessRuleException("The new password must be different from the current one.");
        }
        u.setPassword(passwords.hash(req.getNewPassword()));
    }

    // ---------------------------------------------------------------- helpers

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private void checkEmail(String email) {
        if (email.length() > 120 || !EMAIL.matcher(email).matches()) {
            throw new BusinessRuleException("Enter a valid email address.");
        }
    }

    private void checkName(String fullName) {
        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new BusinessRuleException("Full name is required (up to 100 characters).");
        }
    }

    private void checkPhone(String phone) {
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            throw new BusinessRuleException("Phone may contain digits, spaces, + ( ) - only (7-20 characters).");
        }
    }

    private SessionView view(User u) {
        return new SessionView(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRoles().stream().map(Role::getName).sorted(Comparator.naturalOrder()).toList(), null);
    }
}
