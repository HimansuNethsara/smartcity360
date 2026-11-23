package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.*;
import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintStatus;
import lk.jiat.smartcity360.domain.enums.EscalationReason;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.pattern.chain.EscalationChain;
import lk.jiat.smartcity360.pattern.chain.RoutingDecision;
import lk.jiat.smartcity360.pattern.chain.RoutingRequest;
import lk.jiat.smartcity360.repository.*;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;
import lk.jiat.smartcity360.domain.enums.NotificationType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Stateless
public class ComplaintService {

    private static final Map<ComplaintStatus, Set<ComplaintStatus>> TRANSITIONS = Map.of(
            ComplaintStatus.OPEN, EnumSet.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED),
            ComplaintStatus.ESCALATED, EnumSet.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED),
            ComplaintStatus.IN_PROGRESS, EnumSet.of(ComplaintStatus.RESOLVED),
            ComplaintStatus.RESOLVED, EnumSet.of(ComplaintStatus.CLOSED),
            ComplaintStatus.CLOSED, EnumSet.noneOf(ComplaintStatus.class));

    private final EscalationChain chain = EscalationChain.standard();

    @EJB
    private ComplaintRepository complaintRepository;
    @EJB
    private ComplaintEscalationRepository escalationRepository;
    @EJB
    private EscalationPolicyRepository policyRepository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private ZoneRepository zoneRepository;
    @EJB
    private StreetlightRepository streetlightRepository;
    @EJB
    private lk.jiat.smartcity360.messaging.NotificationPublisher notifier;

    // ---------------------------------------------------------------- reads

    public List<ComplaintView> list(ComplaintStatus status, ComplaintLevel level, Long onlyCitizenId) {
        LocalDateTime now = now();
        return complaintRepository.findAllNewestFirst().stream()
                .filter(c -> onlyCitizenId == null || c.getCitizen().getId().equals(onlyCitizenId))
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> level == null || c.getCurrentLevel() == level)
                .map(c -> view(c, now, false, null))
                .toList();
    }

    public ComplaintView get(Long id, Long onlyCitizenId) {
        Complaint c = require(id);
        if (onlyCitizenId != null && !c.getCitizen().getId().equals(onlyCitizenId)) {
            throw new ResourceNotFoundException("Complaint " + id + " not found");
        }
        return view(c, now(), true, null);
    }

    // ---------------------------------------------------------------- commands

    public ComplaintView submit(ComplaintRequest req) {
        String title = req.getTitle() == null ? "" : req.getTitle().trim();
        String description = req.getDescription() == null ? "" : req.getDescription().trim();
        if (title.length() < 5 || title.length() > 150) {
            throw new BusinessRuleException("Title must be 5 to 150 characters");
        }
        if (description.isEmpty() || description.length() > 2000) {
            throw new BusinessRuleException("Description is required (up to 2000 characters)");
        }
        if (req.getCategory() == null || req.getSeverity() == null) {
            throw new BusinessRuleException("Category and severity are required");
        }
        if (req.getCitizenId() == null) {
            throw new BusinessRuleException("A citizen is required");
        }
        User citizen = userRepository.findById(req.getCitizenId());
        if (citizen == null) {
            throw new ResourceNotFoundException("User " + req.getCitizenId() + " not found");
        }
        if (!Boolean.TRUE.equals(citizen.getEnabled())) {
            throw new BusinessRuleException("This account is disabled");
        }

        Zone zone = null;
        if (req.getZoneId() != null) {
            zone = zoneRepository.findById(req.getZoneId());
            if (zone == null) {
                throw new ResourceNotFoundException("Zone " + req.getZoneId() + " not found");
            }
        }
        Streetlight light = null;
        if (req.getStreetlightId() != null) {
            light = streetlightRepository.findById(req.getStreetlightId());
            if (light == null) {
                throw new ResourceNotFoundException("Streetlight " + req.getStreetlightId() + " not found");
            }
            if (zone == null) {
                zone = light.getZone();
            }
        }

        EscalationPolicy policy = policy(req.getSeverity());
        RoutingDecision decision = chain.route(RoutingRequest.forNewComplaint(req.getSeverity(),
                policy.getStartLevel(), policy.getStaffTimeoutMin(), policy.getSupervisorTimeoutMin()))
                .orElseThrow(() -> new BusinessRuleException("No handler accepted the complaint"));

        LocalDateTime now = now();
        User assignee = pickAssignee(decision.level());
        Complaint complaint = Complaint.builder()
                .citizen(citizen)
                .zone(zone)
                .streetlight(light)
                .category(req.getCategory())
                .title(title)
                .description(description)
                .severity(req.getSeverity())
                .status(ComplaintStatus.OPEN)
                .currentLevel(decision.level())
                .assignedTo(assignee)
                .escalateAt(deadline(now, decision))
                .build();
        complaintRepository.save(complaint);
        logHandover(complaint, null, decision, assignee, decision.note());
        complaintRepository.refresh(complaint);

        notifier.publish(citizen.getId(), NotificationType.COMPLAINT_UPDATE, "Complaint received",
                "Your complaint #" + complaint.getId() + " was received and sent to " + decision.level() + ".");
        if (assignee != null) {
            notifier.publish(assignee.getId(), NotificationType.COMPLAINT_UPDATE, "New complaint assigned",
                    "Complaint #" + complaint.getId() + " (" + req.getSeverity() + "): " + title);
        }

        return view(complaint, now, true, decision.trace());
    }

    /** Manual escalation by staff or a supervisor: moves the complaint one level up. */
    public ComplaintView escalate(Long id, EscalateRequest req) {
        Complaint c = require(id);
        String note = req == null || req.getNote() == null || req.getNote().isBlank() ? null : req.getNote().trim();
        if (note != null && note.length() > 255) {
            throw new BusinessRuleException("Note must be 255 characters or fewer");
        }
        RoutingDecision decision = escalateOne(c, EscalationReason.MANUAL, note);
        complaintRepository.refresh(c);
        return view(c, now(), true, decision.trace());
    }

    public OverdueResult processTimeouts() {
        List<Long> moved = new ArrayList<>();
        for (Complaint c : complaintRepository.findDueForEscalation(now())) {
            if (c.getCurrentLevel() == ComplaintLevel.CITY_MANAGER) {
                c.setEscalateAt(null); // top of the chain: nobody above, stop polling it
                continue;
            }
            escalateOne(c, EscalationReason.TIMEOUT, "No resolution within the allowed time");
            moved.add(c.getId());
        }
        return new OverdueResult(moved.size(), moved);
    }

    public ComplaintView makeOverdue(Long id) {
        Complaint c = require(id);
        if (isDone(c) || c.getCurrentLevel() == ComplaintLevel.CITY_MANAGER) {
            throw new BusinessRuleException("This complaint has no deadline that could pass "
                    + "(it is finished or already with the city manager)");
        }
        c.setEscalateAt(now().minusMinutes(1));
        return view(c, now(), true, null);
    }

    public ComplaintView changeStatus(Long id, StatusRequest req) {
        Complaint c = require(id);
        ComplaintStatus target = req == null ? null : req.getStatus();
        if (target == null) {
            throw new BusinessRuleException("A status is required");
        }
        if (!TRANSITIONS.get(c.getStatus()).contains(target)) {
            throw new BusinessRuleException("A complaint cannot go from " + c.getStatus() + " to " + target);
        }
        c.setStatus(target);
        if (target == ComplaintStatus.RESOLVED) {
            c.setResolvedAt(now());
        }
        if (target == ComplaintStatus.RESOLVED || target == ComplaintStatus.CLOSED) {
            c.setEscalateAt(null);
        }
        notifier.publish(c.getCitizen().getId(), NotificationType.COMPLAINT_UPDATE, "Complaint status changed",
                "Your complaint #" + c.getId() + " is now " + target + ".");
        return view(c, now(), true, null);
    }

    // ---------------------------------------------------------------- helpers

    private RoutingDecision escalateOne(Complaint c, EscalationReason reason, String note) {
        if (isDone(c)) {
            throw new BusinessRuleException("A " + c.getStatus() + " complaint cannot be escalated");
        }
        EscalationPolicy policy = policy(c.getSeverity());
        RoutingDecision decision = chain.route(RoutingRequest.forEscalation(c.getSeverity(), c.getCurrentLevel(),
                reason, policy.getStaffTimeoutMin(), policy.getSupervisorTimeoutMin()))
                .orElseThrow(() -> new BusinessRuleException(
                        "Already with the city manager, the last level in the chain"));

        ComplaintLevel from = c.getCurrentLevel();
        User assignee = pickAssignee(decision.level());
        c.setCurrentLevel(decision.level());
        c.setStatus(ComplaintStatus.ESCALATED);
        c.setAssignedTo(assignee);
        c.setEscalateAt(deadline(now(), decision));
        logHandover(c, from, decision, assignee, note != null ? note : decision.note());

        notifier.publish(c.getCitizen().getId(), NotificationType.COMPLAINT_ESCALATED, "Complaint escalated",
                "Your complaint #" + c.getId() + " was escalated to " + decision.level() + " (" + reason + ").");
        if (assignee != null) {
            notifier.publish(assignee.getId(), NotificationType.COMPLAINT_ESCALATED, "Complaint escalated to you",
                    "Complaint #" + c.getId() + " (" + c.getSeverity() + ") now needs your attention.");
        }

        return decision;
    }

    private void logHandover(Complaint c, ComplaintLevel from, RoutingDecision d, User handler, String note) {
        ComplaintEscalation row = ComplaintEscalation.builder()
                .complaint(c)
                .fromLevel(from)
                .toLevel(d.level())
                .handledBy(handler)
                .reason(d.reason())
                .note(note)
                .build();
        escalationRepository.save(row);
    }

    private User pickAssignee(ComplaintLevel level) {
        User best = null;
        long bestLoad = Long.MAX_VALUE;
        for (User u : userRepository.findEnabledByRole(level.name())) {
            long load = complaintRepository.countOpenByAssignee(u.getId());
            if (load < bestLoad) {
                best = u;
                bestLoad = load;
            }
        }
        return best;
    }

    private EscalationPolicy policy(lk.jiat.smartcity360.domain.enums.ComplaintSeverity severity) {
        return policyRepository.findBySeverity(severity)
                .orElseThrow(() -> new BusinessRuleException("No escalation policy for severity " + severity));
    }

    private LocalDateTime deadline(LocalDateTime from, RoutingDecision d) {
        return d.timeoutMinutes() == null ? null : from.plusMinutes(d.timeoutMinutes());
    }

    private boolean isDone(Complaint c) {
        return c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED;
    }

    private Complaint require(Long id) {
        Complaint c = complaintRepository.findById(id);
        if (c == null) {
            throw new ResourceNotFoundException("Complaint " + id + " not found");
        }
        return c;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private ComplaintView view(Complaint c, LocalDateTime now, boolean withHistory, List<String> trace) {
        List<EscalationEntryView> history = null;
        if (withHistory) {
            history = escalationRepository.findByComplaint(c.getId()).stream()
                    .map(e -> new EscalationEntryView(e.getId(),
                            e.getFromLevel() == null ? null : e.getFromLevel().name(),
                            e.getToLevel().name(),
                            e.getHandledBy() == null ? null : e.getHandledBy().getFullName(),
                            e.getReason().name(), e.getNote(),
                            e.getCreatedAt() == null ? null : e.getCreatedAt().toString()))
                    .toList();
        }
        Long minutesLeft = c.getEscalateAt() == null || isDone(c)
                ? null : Duration.between(now, c.getEscalateAt()).toMinutes();
        return new ComplaintView(c.getId(), c.getCitizen().getFullName(),
                c.getZone() == null ? null : c.getZone().getName(),
                c.getStreetlight() == null ? null : c.getStreetlight().getId(),
                c.getCategory().name(), c.getTitle(), c.getDescription(), c.getSeverity().name(),
                c.getStatus().name(), c.getCurrentLevel().name(),
                c.getAssignedTo() == null ? null : c.getAssignedTo().getFullName(),
                c.getEscalateAt() == null ? null : c.getEscalateAt().toString(), minutesLeft,
                c.getCreatedAt() == null ? null : c.getCreatedAt().toString(),
                c.getResolvedAt() == null ? null : c.getResolvedAt().toString(), history, trace);
    }
}
