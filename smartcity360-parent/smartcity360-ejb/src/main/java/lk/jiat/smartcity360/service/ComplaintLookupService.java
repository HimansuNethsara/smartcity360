package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.dto.ComplaintOptions;
import lk.jiat.smartcity360.dto.EscalationPolicyView;
import lk.jiat.smartcity360.dto.ZoneOption;
import lk.jiat.smartcity360.repository.EscalationPolicyRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.repository.ZoneRepository;

import java.util.Comparator;

@Stateless
public class ComplaintLookupService {

    @EJB
    private UserRepository userRepository;
    @EJB
    private ZoneRepository zoneRepository;
    @EJB
    private EscalationPolicyRepository policyRepository;

    public ComplaintOptions options() {
        return new ComplaintOptions(
                userRepository.findEnabledByRole("CITIZEN").stream()
                        .map(u -> new ZoneOption(u.getId(), u.getFullName(), u.getUsername()))
                        .toList(),
                zoneRepository.findAll().stream()
                        .sorted(Comparator.comparing(z -> z.getName()))
                        .map(z -> new ZoneOption(z.getId(), z.getName() + " (" + z.getZoneType() + ")", z.getCode()))
                        .toList(),
                policyRepository.findAll().stream()
                        .sorted(Comparator.comparing(p -> p.getSeverity()))
                        .map(p -> new EscalationPolicyView(p.getSeverity().name(), p.getStartLevel().name(),
                                p.getStaffTimeoutMin(), p.getSupervisorTimeoutMin()))
                        .toList());
    }
}
