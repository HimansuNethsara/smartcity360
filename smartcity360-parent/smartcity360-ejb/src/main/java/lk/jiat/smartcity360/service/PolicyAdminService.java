package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.CityPolicy;
import lk.jiat.smartcity360.domain.PolicyHistory;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.pattern.interpreter.Expression;
import lk.jiat.smartcity360.pattern.interpreter.PolicyParseException;
import lk.jiat.smartcity360.pattern.interpreter.PolicyParser;
import lk.jiat.smartcity360.pattern.interpreter.PolicyProgram;
import lk.jiat.smartcity360.pattern.interpreter.PolicyVocabulary;
import lk.jiat.smartcity360.pattern.memento.PolicyCaretaker;
import lk.jiat.smartcity360.pattern.memento.PolicyDocument;
import lk.jiat.smartcity360.pattern.memento.PolicyMemento;
import lk.jiat.smartcity360.repository.CityPolicyRepository;
import lk.jiat.smartcity360.repository.PolicyHistoryRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.util.List;

@Stateless
public class PolicyAdminService {

    private final PolicyVocabulary vocabulary = PolicyVocabulary.standard();

    @EJB
    private CityPolicyRepository policyRepository;
    @EJB
    private PolicyHistoryRepository historyRepository;
    @EJB
    private UserRepository userRepository;

    // ---------------------------------------------------------------- reads

    public List<PolicyView> list() {
        return policyRepository.findAllByName().stream().map(this::view).toList();
    }

    public PolicyView get(Long id) {
        return view(require(id));
    }

    public List<PolicyVersionView> versions(Long id) {
        CityPolicy p = require(id);
        List<PolicyHistory> rows = historyRepository.findByPolicyOrdered(id);
        boolean[] currentTaken = {false};
        return rows.stream().map(h -> {
            boolean current = !currentTaken[0] && h.getDslText().equals(p.getDslText());
            currentTaken[0] |= current;
            return new PolicyVersionView(h.getVersionNo(), h.getDslText(), h.getChangedBy().getFullName(),
                    h.getChangedAt() == null ? null : h.getChangedAt().toString(), current);
        }).toList();
    }

    public ValidationView validate(ValidateRequest req) {
        String text = req == null ? null : req.getDslText();
        try {
            PolicyProgram program = PolicyParser.parse(text, vocabulary);
            return new ValidationView(true, program.describe(), null, 0, ast(program));
        } catch (PolicyParseException e) {
            return new ValidationView(false, null, e.getMessage(), e.getPosition(), null);
        }
    }

    // ---------------------------------------------------------------- commands

    public PolicyView create(PolicyRequest req) {
        User actor = requireActor(req);
        String name = cleanName(req.getName());
        if (policyRepository.findByName(name).isPresent()) {
            throw new BusinessRuleException("A policy named '" + name + "' already exists");
        }
        String dsl = cleanDsl(req.getDslText());

        CityPolicy p = CityPolicy.builder()
                .name(name)
                .description(cleanDescription(req.getDescription()))
                .dslText(dsl)
                .enabled(req.getEnabled() == null || req.getEnabled())
                .createdBy(actor)
                .build();
        policyRepository.save(p);
        writeHistory(p, new PolicyDocument(dsl).save(1), actor);
        return view(reload(p));
    }

    public PolicyView update(Long id, PolicyRequest req) {
        User actor = requireActor(req);
        CityPolicy p = require(id);
        if (req.getVersion() != null && !req.getVersion().equals(p.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Policy " + id + " was changed by someone else (you had version " + req.getVersion() + ")");
        }
        String name = cleanName(req.getName());
        policyRepository.findByName(name).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new BusinessRuleException("A policy named '" + name + "' already exists");
            }
        });
        String dsl = cleanDsl(req.getDslText());

        if (!dsl.equals(p.getDslText())) {
            PolicyCaretaker caretaker = caretaker(p);
            ensureBaseline(p, caretaker);
            PolicyDocument doc = new PolicyDocument(p.getDslText());
            doc.edit(dsl);
            PolicyMemento m = doc.save(caretaker.nextVersionNo());
            writeHistory(p, m, actor);
            p.setDslText(doc.getDslText());
        }
        p.setName(name);
        p.setDescription(cleanDescription(req.getDescription()));
        if (req.getEnabled() != null) {
            p.setEnabled(req.getEnabled());
        }
        return view(reload(p));
    }

    public PolicyView setEnabled(Long id, ToggleRequest req) {
        if (req == null || req.getEnabled() == null) {
            throw new BusinessRuleException("enabled must be true or false");
        }
        CityPolicy p = require(id);
        p.setEnabled(req.getEnabled());
        return view(reload(p));
    }

    public PolicyView rollback(Long id, RollbackRequest req) {
        if (req == null || req.getVersionNo() == null) {
            throw new BusinessRuleException("A version number is required");
        }
        User actor = userRepository.findById(req.getActorId() == null ? -1L : req.getActorId());
        if (actor == null) {
            throw new BusinessRuleException("Say who is restoring this version (a valid actorId)");
        }
        CityPolicy p = require(id);
        PolicyCaretaker caretaker = caretaker(p);
        PolicyMemento target = caretaker.get(req.getVersionNo()).orElseThrow(() ->
                new ResourceNotFoundException("Policy " + id + " has no version " + req.getVersionNo()));
        if (target.dslText().equals(p.getDslText())) {
            throw new BusinessRuleException("Version " + target.versionNo() + " is already the current text");
        }
        parse(target.dslText());   // an old text that no longer parses must not become current

        ensureBaseline(p, caretaker);
        PolicyDocument doc = new PolicyDocument(p.getDslText());
        doc.restore(target);
        PolicyMemento restored = doc.save(caretaker.nextVersionNo());
        writeHistory(p, restored, actor);
        p.setDslText(doc.getDslText());
        return view(reload(p));
    }

    // ---------------------------------------------------------------- helpers

    private PolicyCaretaker caretaker(CityPolicy p) {
        return PolicyCaretaker.of(historyRepository.findByPolicyOrdered(p.getId()).stream()
                .map(h -> new PolicyMemento(h.getVersionNo(), h.getDslText()))
                .toList());
    }

    private void ensureBaseline(CityPolicy p, PolicyCaretaker caretaker) {
        PolicyMemento latest = caretaker.latest();
        if (latest == null || !latest.dslText().equals(p.getDslText())) {
            PolicyMemento baseline = new PolicyDocument(p.getDslText()).save(caretaker.nextVersionNo());
            writeHistory(p, baseline, p.getCreatedBy());
            caretaker.add(baseline);
        }
    }

    private void writeHistory(CityPolicy p, PolicyMemento m, User by) {
        historyRepository.save(PolicyHistory.builder()
                .policy(p)
                .versionNo(m.versionNo())
                .dslText(m.dslText())
                .changedBy(by)
                .build());
    }

    private CityPolicy reload(CityPolicy p) {
        policyRepository.flush();
        policyRepository.refresh(p);
        return p;
    }

    private PolicyProgram parse(String dsl) {
        try {
            return PolicyParser.parse(dsl, vocabulary);
        } catch (PolicyParseException e) {
            throw new BusinessRuleException(e.getMessage() + (e.getPosition() > 0 ? " (at character " + e.getPosition() + ")" : ""));
        }
    }

    private String cleanDsl(String dsl) {
        String text = dsl == null ? "" : dsl.trim();
        parse(text);
        return text;
    }

    private String cleanName(String name) {
        String n = name == null ? "" : name.trim();
        if (n.length() < 3 || n.length() > 80) {
            throw new BusinessRuleException("The name must be 3 to 80 characters");
        }
        return n;
    }

    private String cleanDescription(String d) {
        if (d == null || d.isBlank()) {
            return null;
        }
        if (d.trim().length() > 255) {
            throw new BusinessRuleException("The description must be 255 characters or fewer");
        }
        return d.trim();
    }

    private User requireActor(PolicyRequest req) {
        if (req == null) {
            throw new BusinessRuleException("A request body is required");
        }
        User actor = req.getActorId() == null ? null : userRepository.findById(req.getActorId());
        if (actor == null) {
            throw new BusinessRuleException("Say who is making this change (a valid actorId)");
        }
        return actor;
    }

    private CityPolicy require(Long id) {
        CityPolicy p = policyRepository.findById(id);
        if (p == null) {
            throw new ResourceNotFoundException("Policy " + id + " not found");
        }
        return p;
    }

    private PolicyView view(CityPolicy p) {
        String canonical = null;
        String error = null;
        AstNodeView tree = null;
        try {
            PolicyProgram program = PolicyParser.parse(p.getDslText(), vocabulary);
            canonical = program.describe();
            tree = ast(program);
        } catch (PolicyParseException e) {
            error = e.getMessage();
        }
        return new PolicyView(p.getId(), p.getName(), p.getDescription(), p.getDslText(), canonical, error == null,
                error, Boolean.TRUE.equals(p.getEnabled()), p.getVersion(), p.getCreatedBy().getFullName(),
                p.getUpdatedAt() == null ? null : p.getUpdatedAt().toString(), tree);
    }

    private AstNodeView ast(PolicyProgram program) {
        return new AstNodeView("IF … THEN " + program.action().result(), List.of(node(program.condition())));
    }

    private AstNodeView node(Expression e) {
        return new AstNodeView(e.label(), e.children().stream().map(this::node).toList());
    }
}
