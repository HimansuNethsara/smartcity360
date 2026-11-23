package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.AuditLog;
import lk.jiat.smartcity360.domain.Role;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.dto.ResetPasswordRequest;
import lk.jiat.smartcity360.dto.UserCreateRequest;
import lk.jiat.smartcity360.dto.UserUpdateRequest;
import lk.jiat.smartcity360.dto.UserView;
import lk.jiat.smartcity360.repository.AuditLogRepository;
import lk.jiat.smartcity360.repository.RoleRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.security.PasswordService;
import lk.jiat.smartcity360.security.RoleNames;
import lk.jiat.smartcity360.security.JwtService;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.util.*;
import java.util.regex.Pattern;

@Stateless
public class UserAdminService {

    private static final String PROTECTED_USERNAME = "admin";
    private static final List<String> ROLE_ORDER = List.of(RoleNames.ADMIN, RoleNames.CITY_MANAGER,
            RoleNames.SUPERVISOR, RoleNames.STAFF, RoleNames.CITIZEN);

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern PHONE = Pattern.compile("[0-9+() -]{7,20}");

    @EJB
    private UserRepository userRepository;
    @EJB
    private RoleRepository roleRepository;
    @EJB
    private PasswordService passwords;
    @EJB
    private JwtService jwtService;
    @EJB
    private AuditLogRepository auditLogRepository;

    // ---------------------------------------------------------------- queries

    public List<UserView> listUsers(String roleFilter, Boolean enabledFilter, String search) {
        String role = clean(roleFilter).toUpperCase(Locale.ROOT);
        String q = clean(search).toLowerCase(Locale.ROOT);
        return userRepository.findAll().stream()
                .filter(u -> enabledFilter == null || enabledFilter.equals(u.getEnabled()))
                .filter(u -> role.isEmpty() || u.hasRole(role))
                .filter(u -> q.isEmpty() || contains(u.getUsername(), q) || contains(u.getFullName(), q)
                        || contains(u.getEmail(), q))
                .sorted(Comparator.comparing(User::getId))
                .map(this::view)
                .toList();
    }

    // ---------------------------------------------------------------- commands

    public UserView create(UserCreateRequest req, Long callerId) {
        if (req == null) {
            throw new BusinessRuleException("Request body is required.");
        }
        String username = clean(req.getUsername());
        String email = clean(req.getEmail()).toLowerCase(Locale.ROOT);
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
        Set<Role> roles = resolveRoles(req.getRoles());

        User u = User.builder()
                .username(username)
                .password(passwords.hash(req.getPassword()))
                .email(email)
                .fullName(fullName)
                .phone(phone.isEmpty() ? null : phone)
                .build();
        u.getRoles().addAll(roles);
        userRepository.save(u);

        audit(callerId, "USER_CREATE", u.getId(),
                json(Map.<String, Object>of("username", username, "roles", roleNames(roles))));
        return view(u);
    }

    /** Partial update: a null field means "leave unchanged". */
    public UserView update(Long id, UserUpdateRequest req, Long callerId) {
        if (req == null) {
            throw new BusinessRuleException("Request body is required.");
        }
        User u = requireUser(id);
        List<String> changed = new ArrayList<>();

        if (req.getFullName() != null) {
            String v = clean(req.getFullName());
            checkName(v);
            if (!v.equals(u.getFullName())) {
                u.setFullName(v);
                changed.add("fullName");
            }
        }
        if (req.getEmail() != null) {
            String v = clean(req.getEmail()).toLowerCase(Locale.ROOT);
            checkEmail(v);
            if (!v.equalsIgnoreCase(u.getEmail())) {
                if (userRepository.emailTaken(v)) {
                    throw new BusinessRuleException("That email is already registered.");
                }
                u.setEmail(v);
                changed.add("email");
            }
        }
        if (req.getPhone() != null) {
            String v = clean(req.getPhone());
            checkPhone(v);
            String phone = v.isEmpty() ? null : v;
            if (!Objects.equals(phone, u.getPhone())) {
                u.setPhone(phone);
                changed.add("phone");
            }
        }
        if (!changed.isEmpty()) {
            audit(callerId, "USER_UPDATE", id, json(Map.<String, Object>of("fields", changed)));
        }
        if (req.getEnabled() != null) {
            applyEnabled(u, req.getEnabled(), callerId);
        }
        if (req.getRoles() != null) {
            applyRoles(u, req.getRoles(), callerId);
        }
        return view(u);
    }

    public void resetPassword(Long id, ResetPasswordRequest req, Long callerId) {
        User u = requireUser(id);
        boolean self = u.getId().equals(callerId);
        if (isProtected(u) && !self) {
            throw new BusinessRuleException("Only the admin user can reset the admin password.");
        }
        String plain = req == null ? null : req.getNewPassword();
        passwords.requireStrong(plain);
        u.setPassword(passwords.hash(plain));
        audit(callerId, "USER_RESET_PASSWORD", id, json(Map.<String, Object>of("self", self)));
        jwtService.revokeUser(u.getId());          // old sessions must not survive a password reset
    }

    public UserView setEnabled(Long id, Boolean enabled, Long callerId) {
        if (enabled == null) {
            throw new BusinessRuleException("The enabled flag is required.");
        }
        User u = requireUser(id);
        applyEnabled(u, enabled, callerId);
        return view(u);
    }

    public UserView setRoles(Long id, List<String> roles, Long callerId) {
        User u = requireUser(id);
        applyRoles(u, roles, callerId);
        return view(u);
    }

    // ---------------------------------------------------------------- rules

    private void applyEnabled(User u, boolean enabled, Long callerId) {
        boolean was = Boolean.TRUE.equals(u.getEnabled());
        if (!enabled) {
            if (u.getId().equals(callerId)) {
                throw new BusinessRuleException("You cannot disable your own account.");
            }
            if (isProtected(u)) {
                throw new BusinessRuleException("The admin account cannot be disabled.");
            }
        }
        if (was == enabled) {
            return;
        }
        u.setEnabled(enabled);
        audit(callerId, "USER_SET_ENABLED", u.getId(),
                json(Map.<String, Object>of("enabled", enabled, "was", was)));
        if (!enabled) {
            jwtService.revokeUser(u.getId());      // sign the disabled user out everywhere
        }
    }

    private void applyRoles(User u, List<String> names, Long callerId) {
        Set<Role> wanted = resolveRoles(names);
        boolean keepsAdmin = wanted.stream().anyMatch(r -> RoleNames.ADMIN.equals(r.getName()));
        if (!keepsAdmin) {
            if (isProtected(u)) {
                throw new BusinessRuleException("The admin account must keep the ADMIN role.");
            }
            if (u.getId().equals(callerId)) {
                throw new BusinessRuleException("You cannot remove the ADMIN role from yourself.");
            }
        }
        List<String> before = roleNames(u.getRoles());
        List<String> after = roleNames(wanted);
        if (before.equals(after)) {
            return;
        }
        u.getRoles().clear();
        u.getRoles().addAll(wanted);
        audit(callerId, "USER_SET_ROLES", u.getId(), json(Map.<String, Object>of("from", before, "to", after)));
        jwtService.revokeUser(u.getId());          // roles live inside the token, so force a fresh login
    }

    private boolean isProtected(User u) {
        return PROTECTED_USERNAME.equalsIgnoreCase(u.getUsername());
    }

    private Set<Role> resolveRoles(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new BusinessRuleException("Choose at least one role.");
        }
        Set<Role> out = new LinkedHashSet<>();
        for (String raw : names) {
            String name = clean(raw).toUpperCase(Locale.ROOT);
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new BusinessRuleException("Unknown role '" + raw + "'."));
            out.add(role);
        }
        return out;
    }

    // ---------------------------------------------------------------- helpers

    private User requireUser(Long id) {
        User u = id == null ? null : userRepository.findByIdWithRoles(id);
        if (u == null) {
            throw new ResourceNotFoundException("User " + id + " not found");
        }
        return u;
    }

    private void audit(Long callerId, String action, Long targetId, String detailJson) {
        User caller = callerId == null ? null : userRepository.findById(callerId);
        auditLogRepository.save(AuditLog.builder()
                .user(caller)
                .action(action)
                .entityType("USER")
                .entityId(targetId)
                .detailJson(detailJson)
                .build());
    }

    private List<String> roleNames(Collection<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .sorted(Comparator.comparingInt(n -> {
                    int i = ROLE_ORDER.indexOf(n);
                    return i < 0 ? ROLE_ORDER.size() : i;
                }))
                .toList();
    }

    private UserView view(User u) {
        return new UserView(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getEnabled(), roleNames(u.getRoles()),
                u.getCreatedAt() == null ? null : u.getCreatedAt().toString());
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private boolean contains(String value, String lowerNeedle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerNeedle);
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

    private String json(Map<String, Object> values) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : new TreeMap<>(values).entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append("\":");
            appendValue(sb, e.getValue());
        }
        return sb.append('}').toString();
    }

    private void appendValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof Boolean || v instanceof Number) {
            sb.append(v);
        } else if (v instanceof Collection<?> items) {
            sb.append('[');
            boolean first = true;
            for (Object item : items) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                appendValue(sb, item);
            }
            sb.append(']');
        } else {
            sb.append('"');
            for (char c : String.valueOf(v).toCharArray()) {
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (c < 0x20) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                    }
                }
            }
            sb.append('"');
        }
    }
}