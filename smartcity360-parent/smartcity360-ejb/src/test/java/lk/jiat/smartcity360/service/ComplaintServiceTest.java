package lk.jiat.smartcity360.service;

import lk.jiat.smartcity360.domain.Complaint;
import lk.jiat.smartcity360.domain.ComplaintEscalation;
import lk.jiat.smartcity360.domain.EscalationPolicy;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.domain.enums.ComplaintCategory;
import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import lk.jiat.smartcity360.domain.enums.ComplaintStatus;
import lk.jiat.smartcity360.domain.enums.EscalationReason;
import lk.jiat.smartcity360.dto.ComplaintRequest;
import lk.jiat.smartcity360.dto.ComplaintView;
import lk.jiat.smartcity360.dto.EscalateRequest;
import lk.jiat.smartcity360.dto.OverdueResult;
import lk.jiat.smartcity360.dto.StatusRequest;
import lk.jiat.smartcity360.messaging.NotificationPublisher;
import lk.jiat.smartcity360.repository.ComplaintEscalationRepository;
import lk.jiat.smartcity360.repository.ComplaintRepository;
import lk.jiat.smartcity360.repository.EscalationPolicyRepository;
import lk.jiat.smartcity360.repository.StreetlightRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.repository.ZoneRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock ComplaintRepository complaintRepository;
    @Mock ComplaintEscalationRepository escalationRepository;
    @Mock EscalationPolicyRepository policyRepository;
    @Mock UserRepository userRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock StreetlightRepository streetlightRepository;

    @Mock NotificationPublisher notifier;

    @InjectMocks ComplaintService service;

    // ------------------------------------------------------------------ builders

    private static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private static User user(long id, String name) {
        return User.builder().id(id).username("u" + id).fullName(name).enabled(true).build();
    }

    private static EscalationPolicy policy(ComplaintSeverity severity, ComplaintLevel start) {
        return EscalationPolicy.builder().severity(severity).startLevel(start)
                .staffTimeoutMin((short) 30).supervisorTimeoutMin((short) 60).build();
    }

    private static Complaint complaint(long id, ComplaintStatus status, ComplaintLevel level) {
        return Complaint.builder().id(id).citizen(user(7, "Citizen Seven"))
                .category(ComplaintCategory.STREETLIGHT).title("Light is out").description("Dark street")
                .severity(ComplaintSeverity.MEDIUM).status(status).currentLevel(level)
                .escalateAt(now().plusMinutes(10)).build();
    }

    private static ComplaintRequest request(ComplaintSeverity severity) {
        ComplaintRequest r = new ComplaintRequest();
        r.setCitizenId(7L);
        r.setCategory(ComplaintCategory.NOISE);
        r.setSeverity(severity);
        r.setTitle("  Loud music at night  ");
        r.setDescription("  Every evening after 11  ");
        return r;
    }

    private Complaint savedComplaint() {
        ArgumentCaptor<Complaint> c = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(c.capture());
        return c.getValue();
    }

    private ComplaintEscalation savedHandover() {
        ArgumentCaptor<ComplaintEscalation> c = ArgumentCaptor.forClass(ComplaintEscalation.class);
        verify(escalationRepository).save(c.capture());
        return c.getValue();
    }

    // ------------------------------------------------------------------ submit: routing

    @Test
    @DisplayName("A low-severity complaint starts with staff, with the staff deadline")
    void submitStartsAtStaff() {
        when(userRepository.findById(7L)).thenReturn(user(7, "Citizen Seven"));
        when(policyRepository.findBySeverity(ComplaintSeverity.LOW))
                .thenReturn(Optional.of(policy(ComplaintSeverity.LOW, ComplaintLevel.STAFF)));

        ComplaintView view = service.submit(request(ComplaintSeverity.LOW));

        Complaint saved = savedComplaint();
        assertEquals(ComplaintLevel.STAFF, saved.getCurrentLevel());
        assertEquals(ComplaintStatus.OPEN, saved.getStatus());
        assertEquals("Loud music at night", saved.getTitle(), "title is trimmed");
        assertEquals("Every evening after 11", saved.getDescription(), "description is trimmed");
        assertTrue(saved.getEscalateAt().isAfter(now().plusMinutes(29)));
        assertTrue(saved.getEscalateAt().isBefore(now().plusMinutes(31)));

        ComplaintEscalation row = savedHandover();
        assertNull(row.getFromLevel());
        assertEquals(ComplaintLevel.STAFF, row.getToLevel());
        assertEquals(EscalationReason.INITIAL, row.getReason());
        assertEquals("STAFF", view.getCurrentLevel());
    }

    @Test
    @DisplayName("A severe complaint skips staff and goes to the level its policy names, reason SEVERITY")
    void submitSkipsStaffForSevere() {
        when(userRepository.findById(7L)).thenReturn(user(7, "Citizen Seven"));
        when(policyRepository.findBySeverity(ComplaintSeverity.CRITICAL))
                .thenReturn(Optional.of(policy(ComplaintSeverity.CRITICAL, ComplaintLevel.CITY_MANAGER)));

        service.submit(request(ComplaintSeverity.CRITICAL));

        Complaint saved = savedComplaint();
        assertEquals(ComplaintLevel.CITY_MANAGER, saved.getCurrentLevel());
        assertNull(saved.getEscalateAt(), "nobody is above the city manager, so there is no deadline");
        assertEquals(EscalationReason.SEVERITY, savedHandover().getReason());
    }

    @Test
    @DisplayName("The complaint goes to the least busy enabled person at that level")
    void submitPicksLeastLoadedAssignee() {
        User busy = user(11, "Busy");
        User free = user(12, "Free");
        when(userRepository.findById(7L)).thenReturn(user(7, "Citizen Seven"));
        when(policyRepository.findBySeverity(ComplaintSeverity.HIGH))
                .thenReturn(Optional.of(policy(ComplaintSeverity.HIGH, ComplaintLevel.SUPERVISOR)));
        when(userRepository.findEnabledByRole("SUPERVISOR")).thenReturn(List.of(busy, free));
        when(complaintRepository.countOpenByAssignee(11L)).thenReturn(5L);
        when(complaintRepository.countOpenByAssignee(12L)).thenReturn(2L);

        service.submit(request(ComplaintSeverity.HIGH));

        assertSame(free, savedComplaint().getAssignedTo());
        assertSame(free, savedHandover().getHandledBy());
    }

    @Test
    @DisplayName("With nobody available at the level the complaint is still saved, unassigned")
    void submitWithoutAssignee() {
        when(userRepository.findById(7L)).thenReturn(user(7, "Citizen Seven"));
        when(policyRepository.findBySeverity(ComplaintSeverity.LOW))
                .thenReturn(Optional.of(policy(ComplaintSeverity.LOW, ComplaintLevel.STAFF)));

        service.submit(request(ComplaintSeverity.LOW));

        assertNull(savedComplaint().getAssignedTo());
    }

    // ------------------------------------------------------------------ submit: validation

    @Test
    @DisplayName("Title and description rules are enforced before anything is saved")
    void submitValidatesText() {
        ComplaintRequest shortTitle = request(ComplaintSeverity.LOW);
        shortTitle.setTitle("abc");
        assertThrows(BusinessRuleException.class, () -> service.submit(shortTitle));

        ComplaintRequest noDescription = request(ComplaintSeverity.LOW);
        noDescription.setDescription("   ");
        assertThrows(BusinessRuleException.class, () -> service.submit(noDescription));

        ComplaintRequest longDescription = request(ComplaintSeverity.LOW);
        longDescription.setDescription("x".repeat(2001));
        assertThrows(BusinessRuleException.class, () -> service.submit(longDescription));

        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Category, severity and citizen are required")
    void submitRequiresFields() {
        ComplaintRequest noCategory = request(ComplaintSeverity.LOW);
        noCategory.setCategory(null);
        assertThrows(BusinessRuleException.class, () -> service.submit(noCategory));

        ComplaintRequest noSeverity = request(null);
        assertThrows(BusinessRuleException.class, () -> service.submit(noSeverity));

        ComplaintRequest noCitizen = request(ComplaintSeverity.LOW);
        noCitizen.setCitizenId(null);
        assertThrows(BusinessRuleException.class, () -> service.submit(noCitizen));
    }

    @Test
    @DisplayName("An unknown citizen is 'not found'; a disabled citizen is refused")
    void submitChecksCitizen() {
        assertThrows(ResourceNotFoundException.class, () -> service.submit(request(ComplaintSeverity.LOW)));

        User disabled = user(7, "Citizen Seven");
        disabled.setEnabled(false);
        when(userRepository.findById(7L)).thenReturn(disabled);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.submit(request(ComplaintSeverity.LOW)));
        assertTrue(ex.getMessage().contains("disabled"));
    }

    @Test
    @DisplayName("An unknown zone or a missing escalation policy stops the submission")
    void submitChecksZoneAndPolicy() {
        when(userRepository.findById(7L)).thenReturn(user(7, "Citizen Seven"));

        ComplaintRequest badZone = request(ComplaintSeverity.LOW);
        badZone.setZoneId(99L);
        assertThrows(ResourceNotFoundException.class, () -> service.submit(badZone));

        when(policyRepository.findBySeverity(ComplaintSeverity.LOW)).thenReturn(Optional.empty());
        assertThrows(BusinessRuleException.class, () -> service.submit(request(ComplaintSeverity.LOW)));
        verify(complaintRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ manual escalation

    @Test
    @DisplayName("Manual escalation moves the complaint one level up and records who and why")
    void escalateMovesOneLevel() {
        Complaint c = complaint(1, ComplaintStatus.OPEN, ComplaintLevel.STAFF);
        User supervisor = user(21, "Super Visor");
        when(complaintRepository.findById(1L)).thenReturn(c);
        when(policyRepository.findBySeverity(ComplaintSeverity.MEDIUM))
                .thenReturn(Optional.of(policy(ComplaintSeverity.MEDIUM, ComplaintLevel.STAFF)));
        when(userRepository.findEnabledByRole("SUPERVISOR")).thenReturn(List.of(supervisor));

        EscalateRequest req = new EscalateRequest();
        req.setNote("  customer called twice  ");
        service.escalate(1L, req);

        assertEquals(ComplaintLevel.SUPERVISOR, c.getCurrentLevel());
        assertEquals(ComplaintStatus.ESCALATED, c.getStatus());
        assertSame(supervisor, c.getAssignedTo());
        assertTrue(c.getEscalateAt().isAfter(now().plusMinutes(59)), "supervisor timeout is 60 minutes");

        ComplaintEscalation row = savedHandover();
        assertEquals(ComplaintLevel.STAFF, row.getFromLevel());
        assertEquals(ComplaintLevel.SUPERVISOR, row.getToLevel());
        assertEquals(EscalationReason.MANUAL, row.getReason());
        assertEquals("customer called twice", row.getNote());
    }

    @Test
    @DisplayName("Finished complaints cannot be escalated")
    void escalateRefusesFinished() {
        when(complaintRepository.findById(1L)).thenReturn(complaint(1, ComplaintStatus.RESOLVED, ComplaintLevel.STAFF));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.escalate(1L, null));
        assertTrue(ex.getMessage().contains("cannot be escalated"));
    }

    @Test
    @DisplayName("A complaint already with the city manager cannot go higher")
    void escalateRefusesPastManager() {
        when(complaintRepository.findById(1L)).thenReturn(complaint(1, ComplaintStatus.OPEN, ComplaintLevel.CITY_MANAGER));
        when(policyRepository.findBySeverity(ComplaintSeverity.MEDIUM))
                .thenReturn(Optional.of(policy(ComplaintSeverity.MEDIUM, ComplaintLevel.STAFF)));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.escalate(1L, null));
        assertTrue(ex.getMessage().contains("last level"));
    }

    @Test
    @DisplayName("Over-long notes and unknown complaints are rejected")
    void escalateValidates() {
        when(complaintRepository.findById(1L)).thenReturn(complaint(1, ComplaintStatus.OPEN, ComplaintLevel.STAFF));
        EscalateRequest longNote = new EscalateRequest();
        longNote.setNote("n".repeat(256));
        assertThrows(BusinessRuleException.class, () -> service.escalate(1L, longNote));
        assertThrows(ResourceNotFoundException.class, () -> service.escalate(404L, null));
    }

    // ------------------------------------------------------------------ timeouts

    @Test
    @DisplayName("Overdue complaints are escalated with reason TIMEOUT; those at the top just stop being polled")
    void processTimeouts() {
        Complaint overdue = complaint(1, ComplaintStatus.OPEN, ComplaintLevel.STAFF);
        Complaint atTop = complaint(2, ComplaintStatus.ESCALATED, ComplaintLevel.CITY_MANAGER);
        when(complaintRepository.findDueForEscalation(any())).thenReturn(List.of(overdue, atTop));
        when(policyRepository.findBySeverity(ComplaintSeverity.MEDIUM))
                .thenReturn(Optional.of(policy(ComplaintSeverity.MEDIUM, ComplaintLevel.STAFF)));

        OverdueResult result = service.processTimeouts();

        assertEquals(1, result.getEscalated());
        assertEquals(List.of(1L), result.getComplaintIds());
        assertEquals(ComplaintLevel.SUPERVISOR, overdue.getCurrentLevel());
        assertEquals(ComplaintLevel.CITY_MANAGER, atTop.getCurrentLevel());
        assertNull(atTop.getEscalateAt());
        assertEquals(EscalationReason.TIMEOUT, savedHandover().getReason());
    }

    @Test
    @DisplayName("Nothing due means nothing happens")
    void processTimeoutsNothingDue() {
        when(complaintRepository.findDueForEscalation(any())).thenReturn(List.of());
        OverdueResult result = service.processTimeouts();
        assertEquals(0, result.getEscalated());
        assertTrue(result.getComplaintIds().isEmpty());
        verify(escalationRepository, never()).save(any());
    }

    @Test
    @DisplayName("makeOverdue pulls the deadline into the past, but not for finished or top-level complaints")
    void makeOverdue() {
        Complaint open = complaint(1, ComplaintStatus.OPEN, ComplaintLevel.STAFF);
        when(complaintRepository.findById(1L)).thenReturn(open);
        service.makeOverdue(1L);
        assertTrue(open.getEscalateAt().isBefore(now()));

        when(complaintRepository.findById(2L)).thenReturn(complaint(2, ComplaintStatus.CLOSED, ComplaintLevel.STAFF));
        assertThrows(BusinessRuleException.class, () -> service.makeOverdue(2L));
        when(complaintRepository.findById(3L)).thenReturn(complaint(3, ComplaintStatus.OPEN, ComplaintLevel.CITY_MANAGER));
        assertThrows(BusinessRuleException.class, () -> service.makeOverdue(3L));
    }

    // ------------------------------------------------------------------ status changes

    @ParameterizedTest
    @CsvSource({
            "OPEN,         IN_PROGRESS",
            "OPEN,         RESOLVED",
            "ESCALATED,    IN_PROGRESS",
            "ESCALATED,    RESOLVED",
            "IN_PROGRESS,  RESOLVED",
            "RESOLVED,     CLOSED"})
    @DisplayName("Allowed status changes go through")
    void allowedTransitions(ComplaintStatus from, ComplaintStatus to) {
        Complaint c = complaint(1, from, ComplaintLevel.STAFF);
        when(complaintRepository.findById(1L)).thenReturn(c);
        StatusRequest req = new StatusRequest();
        req.setStatus(to);

        service.changeStatus(1L, req);

        assertEquals(to, c.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN,         ESCALATED",
            "OPEN,         CLOSED",
            "IN_PROGRESS,  OPEN",
            "IN_PROGRESS,  CLOSED",
            "RESOLVED,     OPEN",
            "CLOSED,       OPEN",
            "CLOSED,       RESOLVED"})
    @DisplayName("Disallowed status changes are refused and leave the complaint untouched")
    void forbiddenTransitions(ComplaintStatus from, ComplaintStatus to) {
        Complaint c = complaint(1, from, ComplaintLevel.STAFF);
        when(complaintRepository.findById(1L)).thenReturn(c);
        StatusRequest req = new StatusRequest();
        req.setStatus(to);

        assertThrows(BusinessRuleException.class, () -> service.changeStatus(1L, req));

        assertEquals(from, c.getStatus());
    }

    @Test
    @DisplayName("Resolving stamps the time and clears the deadline; a missing status is refused")
    void resolveClearsDeadline() {
        Complaint c = complaint(1, ComplaintStatus.IN_PROGRESS, ComplaintLevel.STAFF);
        when(complaintRepository.findById(1L)).thenReturn(c);
        StatusRequest req = new StatusRequest();
        req.setStatus(ComplaintStatus.RESOLVED);

        service.changeStatus(1L, req);

        assertNotNull(c.getResolvedAt());
        assertNull(c.getEscalateAt());
        assertThrows(BusinessRuleException.class, () -> service.changeStatus(1L, new StatusRequest()));
    }

    // ------------------------------------------------------------------ reading

    @Test
    @DisplayName("A citizen's list only contains their own complaints; filters narrow it further")
    void listFilters() {
        Complaint mineOpen = complaint(1, ComplaintStatus.OPEN, ComplaintLevel.STAFF);
        Complaint mineDone = complaint(2, ComplaintStatus.RESOLVED, ComplaintLevel.STAFF);
        Complaint someoneElse = complaint(3, ComplaintStatus.OPEN, ComplaintLevel.STAFF);
        someoneElse.setCitizen(user(8, "Other Citizen"));
        when(complaintRepository.findAllNewestFirst()).thenReturn(List.of(mineOpen, mineDone, someoneElse));

        assertEquals(3, service.list(null, null, null).size());
        assertEquals(2, service.list(null, null, 7L).size());
        assertEquals(1, service.list(ComplaintStatus.OPEN, null, 7L).size());
        assertEquals(0, service.list(null, ComplaintLevel.CITY_MANAGER, null).size());
    }

    @Test
    @DisplayName("A citizen asking for someone else's complaint is told it does not exist")
    void getHidesOthersComplaints() {
        when(complaintRepository.findById(1L)).thenReturn(complaint(1, ComplaintStatus.OPEN, ComplaintLevel.STAFF));
        assertThrows(ResourceNotFoundException.class, () -> service.get(1L, 8L));
        assertEquals(1L, service.get(1L, 7L).getId());
        assertEquals(1L, service.get(1L, null).getId());
    }
}
