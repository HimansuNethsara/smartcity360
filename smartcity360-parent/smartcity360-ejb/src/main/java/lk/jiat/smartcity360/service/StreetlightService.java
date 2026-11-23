package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Streetlight;
import lk.jiat.smartcity360.domain.StreetlightType;
import lk.jiat.smartcity360.domain.Zone;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.pattern.flyweight.LightType;
import lk.jiat.smartcity360.pattern.flyweight.LightTypeSpec;
import lk.jiat.smartcity360.pattern.flyweight.MemoryComparison;
import lk.jiat.smartcity360.pattern.flyweight.MemoryReport;
import lk.jiat.smartcity360.pattern.flyweight.StreetLight;
import lk.jiat.smartcity360.repository.StreetlightRepository;
import lk.jiat.smartcity360.repository.StreetlightTypeRepository;
import lk.jiat.smartcity360.repository.ZoneRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Stateless
public class StreetlightService {

    static final int MAX_PER_REQUEST = 20_000;
    static final long MAX_TOTAL = 100_000;
    static final LocalDate GENERATED_MARKER = LocalDate.of(2000, 1, 1);

    @EJB
    private StreetlightRepository lightRepository;
    @EJB
    private StreetlightTypeRepository typeRepository;
    @EJB
    private ZoneRepository zoneRepository;
    @EJB
    private FlyweightRegistry registry;

    // ---------------------------------------------------------------- reads

    public List<ZoneOption> streetZones() {
        return zoneRepository.findByType(ZoneType.STREET).stream()
                .map(z -> new ZoneOption(z.getId(), z.getName(), z.getCode()))
                .toList();
    }

    public StreetlightSummary summary() {
        long watts = 0;
        long lumens = 0;
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (StreetlightStatus s : StreetlightStatus.values()) {
            byStatus.put(s.name(), 0L);
        }
        Map<Integer, Long> perType = new HashMap<>();
        Map<Integer, LightType> typesSeen = new HashMap<>();

        List<Object[]> rows = lightRepository.findAllRows();
        for (Object[] r : rows) {
            LightType type = registry.typeFor((StreetlightType) r[4]);
            StreetLight light = new StreetLight((Long) r[0], (Long) r[1], ((BigDecimal) r[2]).doubleValue(),
                    ((BigDecimal) r[3]).doubleValue(), type, (StreetlightStatus) r[5], (Short) r[6]);
            watts += light.powerWatts();
            lumens += light.lumens();
            byStatus.merge(light.getStatus().name(), 1L, Long::sum);
            perType.merge(type.getId(), 1L, Long::sum);
            typesSeen.put(type.getId(), type);
        }

        List<TypeCount> byType = typesSeen.values().stream()
                .sorted(Comparator.comparing(LightType::getTypeCode))
                .map(t -> new TypeCount(t.getTypeCode(), t.getModel(), t.getLampType().name(),
                        t.getWattage(), perType.get(t.getId())))
                .toList();
        return new StreetlightSummary(rows.size(), watts, lumens, byStatus, byType, registry.stats());
    }

    public StreetlightPage page(Long zoneId, StreetlightStatus status, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);
        List<StreetlightView> items = lightRepository.findPage(zoneId, status, p * s, s).stream()
                .map(this::view)
                .toList();
        return new StreetlightPage(lightRepository.countFiltered(zoneId, status), p, s, items);
    }

    public MemoryView memory(int lights) {
        List<LightTypeSpec> specs = typeRepository.findAll().stream()
                .map(t -> new LightTypeSpec(t.getId(), t.getTypeCode(), t.getModel(), t.getLampType(),
                        t.getWattage(), t.getMaxLumens(), t.getColorTempK(), t.getFirmware()))
                .toList();
        if (specs.isEmpty()) {
            throw new BusinessRuleException("There are no streetlight types to compare");
        }
        MemoryReport r;
        try {
            r = MemoryComparison.compare(specs, lights);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        }
        return new MemoryView(r.lights(), r.distinctTypes(), r.typeObjectsInFlyweight(), r.requests(), r.hits(),
                r.naiveEstimatedBytes(), r.flyweightEstimatedBytes(), r.estimatedSavedPercent(),
                r.naiveMeasuredBytes(), r.flyweightMeasuredBytes());
    }

    // ---------------------------------------------------------------- commands

    public GenerateResult generate(GenerateRequest req) {
        int count = req == null || req.getCount() == null ? 0 : req.getCount();
        if (count < 1 || count > MAX_PER_REQUEST) {
            throw new BusinessRuleException("Count must be between 1 and " + MAX_PER_REQUEST);
        }
        long existing = lightRepository.countAll();
        if (existing + count > MAX_TOTAL) {
            throw new BusinessRuleException("The city is limited to " + MAX_TOTAL + " lights ("
                    + existing + " exist already)");
        }

        List<Zone> zones;
        if (req.getZoneId() != null) {
            Zone zone = zoneRepository.findById(req.getZoneId());
            if (zone == null) {
                throw new ResourceNotFoundException("Zone " + req.getZoneId() + " not found");
            }
            requireStreet(zone);
            zones = List.of(zone);
        } else {
            zones = zoneRepository.findByType(ZoneType.STREET);
            if (zones.isEmpty()) {
                throw new BusinessRuleException("There are no STREET zones to put lights in");
            }
        }
        List<StreetlightType> types = typeRepository.findAll();
        if (types.isEmpty()) {
            throw new BusinessRuleException("There are no streetlight types");
        }

        Random random = new Random();
        List<Object[]> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Zone zone = zones.get(i % zones.size());
            StreetlightType type = types.get(random.nextInt(types.size()));
            int roll = random.nextInt(100);
            StreetlightStatus status = roll < 90 ? StreetlightStatus.ON
                    : roll < 96 ? StreetlightStatus.OFF : StreetlightStatus.FAULTY;
            rows.add(new Object[]{
                    type.getId(), zone.getId(),
                    BigDecimal.valueOf(6.85 + random.nextDouble() * 0.15).setScale(6, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(79.82 + random.nextDouble() * 0.12).setScale(6, RoundingMode.HALF_UP),
                    status.name(), (short) (status == StreetlightStatus.ON ? 40 + random.nextInt(61) : 0),
                    GENERATED_MARKER});
        }
        int inserted = lightRepository.bulkInsert(rows);
        return new GenerateResult(inserted, zones.size(), lightRepository.countAll());
    }

    /** Removes only the lights the generator made. */
    public GenerateResult deleteGenerated() {
        int removed = lightRepository.deleteByInstalledAt(GENERATED_MARKER);
        return new GenerateResult(-removed, 0, lightRepository.countAll());
    }

    public StreetlightView update(Long id, UpdateLightRequest req) {
        Streetlight light = lightRepository.findById(id);
        if (light == null) {
            throw new ResourceNotFoundException("Streetlight " + id + " not found");
        }
        if (req == null || (req.getStatus() == null && req.getBrightness() == null)) {
            throw new BusinessRuleException("Give a status and/or a brightness");
        }
        if (req.getBrightness() != null) {
            if (req.getBrightness() < 0 || req.getBrightness() > 100) {
                throw new BusinessRuleException("Brightness must be between 0 and 100");
            }
            light.setBrightness(req.getBrightness().shortValue());
        }
        if (req.getStatus() != null) {
            light.setStatus(req.getStatus());
        }
        return view(light);
    }

    public PoolStats resetPool() {
        registry.reset();
        return registry.stats();
    }

    // ---------------------------------------------------------------- helpers

    private void requireStreet(Zone zone) {
        if (zone.getZoneType() != ZoneType.STREET) {
            throw new BusinessRuleException("Streetlights can only stand in STREET zones; '"
                    + zone.getName() + "' is a " + zone.getZoneType());
        }
    }

    private StreetlightView view(Streetlight s) {
        LightType type = registry.typeFor(s.getType());
        int brightness = s.getBrightness();
        return new StreetlightView(s.getId(), type.getTypeCode(), type.getModel(), type.getLampType().name(),
                type.getWattage(), s.getZone().getId(), s.getZone().getName(), s.getLatitude(), s.getLongitude(),
                s.getStatus().name(), brightness, type.powerWatts(s.getStatus(), brightness),
                type.lumens(s.getStatus(), brightness));
    }
}