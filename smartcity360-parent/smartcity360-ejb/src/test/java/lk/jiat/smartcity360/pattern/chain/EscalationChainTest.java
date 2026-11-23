package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import lk.jiat.smartcity360.domain.enums.EscalationReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EscalationChainTest {

    private static final int STAFF_MIN = 30;
    private static final int SUP_MIN = 60;

    private final EscalationChain chain = EscalationChain.standard();

    private static RoutingRequest newComplaint(ComplaintLevel start) {
        return RoutingRequest.forNewComplaint(ComplaintSeverity.MEDIUM, start, STAFF_MIN, SUP_MIN);
    }

    private static RoutingRequest escalation(ComplaintLevel from, EscalationReason why) {
        return RoutingRequest.forEscalation(ComplaintSeverity.HIGH, from, why, STAFF_MIN, SUP_MIN);
    }

    // ------------------------------------------------------------------ chain structure

    @Test
    @DisplayName("Standard chain is Staff > Supervisor > City Manager")
    void standardOrder() {
        assertEquals("STAFF>SUPERVISOR>CITY_MANAGER", chain.describe());
    }

    @Test
    @DisplayName("setNext returns the next handler so links can be chained fluently")
    void setNextIsFluent() {
        StaffHandler a = new StaffHandler();
        SupervisorHandler b = new SupervisorHandler();
        assertSame(b, a.setNext(b));
        assertSame(b, a.getNext());
        assertNull(b.getNext());
    }

    // ------------------------------------------------------------------ new complaints

    @Test
    @DisplayName("A new complaint starting at STAFF is handled by staff with the staff timeout")
    void newComplaintAtStaff() {
        RoutingDecision d = chain.route(newComplaint(ComplaintLevel.STAFF)).orElseThrow();
        assertEquals(ComplaintLevel.STAFF, d.level());
        assertEquals(EscalationReason.INITIAL, d.reason());
        assertEquals(STAFF_MIN, d.timeoutMinutes());
        assertEquals(List.of("STAFF: accepted"), d.trace());
    }

    @Test
    @DisplayName("A new complaint whose policy starts at SUPERVISOR skips staff, reason SEVERITY")
    void newComplaintSkipsStaff() {
        RoutingDecision d = chain.route(newComplaint(ComplaintLevel.SUPERVISOR)).orElseThrow();
        assertEquals(ComplaintLevel.SUPERVISOR, d.level());
        assertEquals(EscalationReason.SEVERITY, d.reason());
        assertEquals(SUP_MIN, d.timeoutMinutes());
        assertEquals(List.of("STAFF: passed on (needs SUPERVISOR or higher)", "SUPERVISOR: accepted"), d.trace());
    }

    @Test
    @DisplayName("A complaint starting at CITY_MANAGER lands at the end of the chain with no timeout")
    void newComplaintAtManager() {
        RoutingDecision d = chain.route(newComplaint(ComplaintLevel.CITY_MANAGER)).orElseThrow();
        assertEquals(ComplaintLevel.CITY_MANAGER, d.level());
        assertNull(d.timeoutMinutes(), "nobody is above the city manager, so there is nothing to time out to");
        assertEquals(3, d.trace().size());
    }

    // ------------------------------------------------------------------ escalation

    @ParameterizedTest(name = "{0} escalates to {1}")
    @CsvSource({"STAFF, SUPERVISOR", "SUPERVISOR, CITY_MANAGER"})
    @DisplayName("Escalation always moves exactly one level up")
    void escalatesOneLevel(ComplaintLevel from, ComplaintLevel expected) {
        RoutingDecision d = chain.route(escalation(from, EscalationReason.MANUAL)).orElseThrow();
        assertEquals(expected, d.level());
        assertEquals(EscalationReason.MANUAL, d.reason());
    }

    @Test
    @DisplayName("Timeout escalation from staff gets the supervisor timeout")
    void timeoutEscalationUsesNextLevelTimeout() {
        RoutingDecision d = chain.route(escalation(ComplaintLevel.STAFF, EscalationReason.TIMEOUT)).orElseThrow();
        assertEquals(ComplaintLevel.SUPERVISOR, d.level());
        assertEquals(EscalationReason.TIMEOUT, d.reason());
        assertEquals(SUP_MIN, d.timeoutMinutes());
    }

    @Test
    @DisplayName("Nothing is above the city manager: escalating from there yields no decision")
    void cannotEscalatePastManager() {
        Optional<RoutingDecision> d = chain.route(escalation(ComplaintLevel.CITY_MANAGER, EscalationReason.MANUAL));
        assertTrue(d.isEmpty());
    }

    @Test
    @DisplayName("The trace records every handler the request passed through")
    void traceShowsPath() {
        RoutingDecision d = chain.route(escalation(ComplaintLevel.SUPERVISOR, EscalationReason.MANUAL)).orElseThrow();
        assertEquals(List.of(
                "STAFF: passed on (needs CITY_MANAGER or higher)",
                "SUPERVISOR: passed on (needs CITY_MANAGER or higher)",
                "CITY_MANAGER: accepted"), d.trace());
    }

    @Test
    @DisplayName("Once a handler accepts, later handlers are never called")
    void chainStopsAtFirstAcceptor() {
        AtomicInteger calls = new AtomicInteger();
        ComplaintHandler spy = new ComplaintHandler(ComplaintLevel.CITY_MANAGER) {
            @Override
            protected RoutingDecision accept(RoutingRequest r, List<String> trace) {
                calls.incrementAndGet();
                return new RoutingDecision(getLevel(), r.reason(), null, "spy", trace);
            }
        };
        StaffHandler staff = new StaffHandler();
        staff.setNext(spy);

        Optional<RoutingDecision> d = staff.handle(newComplaint(ComplaintLevel.STAFF), new java.util.ArrayList<>());
        assertEquals(ComplaintLevel.STAFF, d.orElseThrow().level());
        assertEquals(0, calls.get());
    }

    // ------------------------------------------------------------------ RoutingRequest

    @Test
    @DisplayName("minimumLevel: policy start for a new complaint, next level up for an escalation")
    void minimumLevel() {
        assertEquals(ComplaintLevel.SUPERVISOR, newComplaint(ComplaintLevel.SUPERVISOR).minimumLevel());
        assertEquals(ComplaintLevel.SUPERVISOR, escalation(ComplaintLevel.STAFF, EscalationReason.MANUAL).minimumLevel());
        assertEquals(ComplaintLevel.CITY_MANAGER, escalation(ComplaintLevel.SUPERVISOR, EscalationReason.MANUAL).minimumLevel());
        assertNull(escalation(ComplaintLevel.CITY_MANAGER, EscalationReason.MANUAL).minimumLevel());
    }
}
