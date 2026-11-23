package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.EnergyMeter;
import lk.jiat.smartcity360.domain.MeterReading;
import lk.jiat.smartcity360.domain.Zone;
import lk.jiat.smartcity360.domain.enums.MeterStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import lk.jiat.smartcity360.dto.ChangedZone;
import lk.jiat.smartcity360.dto.PowerChangeResult;
import lk.jiat.smartcity360.dto.ZoneNodeView;
import lk.jiat.smartcity360.dto.ZoneTreeView;
import lk.jiat.smartcity360.pattern.composite.*;
import lk.jiat.smartcity360.repository.EnergyMeterRepository;
import lk.jiat.smartcity360.repository.MeterReadingRepository;
import lk.jiat.smartcity360.repository.ZoneRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class ZoneService {

    private static final int USAGE_WINDOW = 24;

    @EJB
    private ZoneRepository zoneRepository;
    @EJB
    private EnergyMeterRepository meterRepository;
    @EJB
    private MeterReadingRepository readingRepository;

    // ---------------------------------------------------------------- reads

    public ZoneTreeView tree() {
        ZoneComponent root = buildRoot();
        ZoneStatsVisitor stats = new ZoneStatsVisitor();
        root.accept(stats);

        Map<String, Integer> perType = new LinkedHashMap<>();
        for (ZoneType t : ZoneType.values()) {
            perType.put(t.name(), stats.getPerType().getOrDefault(t, 0));
        }
        return new ZoneTreeView(nodeView(root.reportUsage()), stats.getTotal(), stats.getOff(),
                stats.getLevels(), perType, USAGE_WINDOW);
    }

    public ZoneNodeView usage(Long zoneId) {
        return nodeView(require(buildRoot(), zoneId).reportUsage());
    }

    // ---------------------------------------------------------------- commands

    public PowerChangeResult shutdown(Long zoneId) {
        return change(zoneId, true);
    }

    public PowerChangeResult restore(Long zoneId) {
        return change(zoneId, false);
    }

    private PowerChangeResult change(Long zoneId, boolean off) {
        ZoneComponent target = require(buildRoot(), zoneId);
        BigDecimal before = target.reportUsage().kwh();

        try {
            if (off) {
                target.shutdownPower();
            } else {
                target.restorePower();
            }
        } catch (IllegalStateException e) {
            throw new BusinessRuleException(e.getMessage());
        }

        ChangedZonesVisitor visitor = new ChangedZonesVisitor();
        target.accept(visitor);

        List<ChangedZone> changed = new ArrayList<>();
        for (ZoneComponent node : visitor.getChanged()) {
            Zone entity = zoneRepository.findById(node.getId());
            entity.setPowerStatus(node.getPower());
            changed.add(new ChangedZone(node.getId(), node.getCode(), node.getName(),
                    node.getType().name(), node.getPower().name()));
        }
        return new PowerChangeResult(zoneId, target.getName(), off ? "SHUTDOWN" : "RESTORE",
                changed.size(), changed, before, target.reportUsage().kwh());
    }

    // ---------------------------------------------------------------- helpers

    private ZoneComponent buildRoot() {
        List<ZoneRow> rows = zoneRepository.findAll().stream()
                .map(z -> new ZoneRow(z.getId(), z.getParent() == null ? null : z.getParent().getId(),
                        z.getZoneType(), z.getCode(), z.getName(), z.getLandUse(), z.getPowerStatus()))
                .toList();

        List<ZoneComponent> roots;
        try {
            roots = ZoneTreeBuilder.build(rows, usageByBuilding());
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Zone data is inconsistent: " + e.getMessage());
        }
        if (roots.size() != 1) {
            throw new BusinessRuleException("Expected exactly one top-level zone but found " + roots.size());
        }
        return roots.get(0);
    }

    private Map<Long, LeafUsage> usageByBuilding() {
        Map<Long, LeafUsage> usage = new HashMap<>();
        for (EnergyMeter meter : meterRepository.findAll()) {
            if (meter.getStatus() != MeterStatus.ACTIVE) {
                continue;
            }
            BigDecimal kwh = BigDecimal.ZERO;
            for (MeterReading r : readingRepository.findRecent(meter.getId(), USAGE_WINDOW)) {
                kwh = kwh.add(r.getKwh());
            }
            usage.merge(meter.getZone().getId(), new LeafUsage(kwh, 1),
                    (a, b) -> new LeafUsage(a.kwh().add(b.kwh()), a.meters() + b.meters()));
        }
        return usage;
    }

    private ZoneComponent require(ZoneComponent root, Long zoneId) {
        ZoneComponent node = root.findById(zoneId);
        if (node == null) {
            throw new ResourceNotFoundException("Zone " + zoneId + " not found");
        }
        return node;
    }

    private ZoneNodeView nodeView(UsageReport r) {
        List<ZoneNodeView> kids = r.children().stream().map(this::nodeView).toList();
        return new ZoneNodeView(r.zoneId(), r.code(), r.name(), r.type().name(), r.power().name(),
                r.kwh(), r.meters(), r.buildings(), kids);
    }
}