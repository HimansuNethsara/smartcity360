package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.EnergyMeter;
import lk.jiat.smartcity360.domain.MeterStreamLayer;
import lk.jiat.smartcity360.domain.Zone;
import lk.jiat.smartcity360.domain.enums.LayerType;
import lk.jiat.smartcity360.domain.enums.MeterStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler.LayerSpec;
import lk.jiat.smartcity360.repository.EnergyMeterRepository;
import lk.jiat.smartcity360.repository.MeterStreamLayerRepository;
import lk.jiat.smartcity360.repository.ZoneRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Stateless
public class MeterAdminService {

    private static final Pattern METER_CODE = Pattern.compile("[A-Za-z0-9_-]{3,30}");
    private static final int MAX_LAYER_ORDER = 20;

    @EJB
    private EnergyMeterRepository meterRepository;
    @EJB
    private MeterStreamLayerRepository layerRepository;
    @EJB
    private ZoneRepository zoneRepository;

    // ---------------------------------------------------------------- reads

    public List<MeterView> listMeters() {
        return meterRepository.findAll().stream()
                .sorted(Comparator.comparing(EnergyMeter::getMeterCode))
                .map(this::toView)
                .toList();
    }

    public MeterView getMeter(Long meterId) {
        return toView(requireMeter(meterId));
    }

    /** BUILDING zones only: the only places a meter may be installed. */
    public List<ZoneOption> buildingZones() {
        return zoneRepository.findByType(ZoneType.BUILDING).stream()
                .map(z -> new ZoneOption(z.getId(), z.getName(), z.getCode()))
                .toList();
    }

    // ---------------------------------------------------------------- meters

    public MeterView createMeter(MeterRequest req) {
        String code = req.getMeterCode() == null ? "" : req.getMeterCode().trim();
        if (!METER_CODE.matcher(code).matches()) {
            throw new BusinessRuleException("Meter code must be 3-30 characters: letters, digits, '-' or '_'");
        }
        if (req.getZoneId() == null) {
            throw new BusinessRuleException("A zone is required");
        }
        if (meterRepository.findByCode(code).isPresent()) {
            throw new BusinessRuleException("Meter code '" + code + "' is already in use");
        }
        Zone zone = zoneRepository.findById(req.getZoneId());
        if (zone == null) {
            throw new ResourceNotFoundException("Zone " + req.getZoneId() + " not found");
        }
        if (zone.getZoneType() != ZoneType.BUILDING) {
            throw new BusinessRuleException("Meters can only be installed in BUILDING zones; '"
                    + zone.getName() + "' is a " + zone.getZoneType());
        }
        EnergyMeter meter = EnergyMeter.builder()
                .meterCode(code)
                .zone(zone)
                .status(MeterStatus.ACTIVE)
                .installedAt(req.getInstalledAt() != null ? req.getInstalledAt() : LocalDate.now(ZoneOffset.UTC))
                .build();
        meterRepository.save(meter);
        return toView(meter);
    }

    // ---------------------------------------------------------------- layers

    public LayerView addLayer(Long meterId, LayerRequest req) {
        EnergyMeter meter = requireMeter(meterId);
        if (req.getLayerType() == null) {
            throw new BusinessRuleException("Layer type is required");
        }
        int order = checkOrder(req.getLayerOrder());
        String config = cleanConfig(req.getConfig());
        boolean enabled = req.getEnabled() == null || req.getEnabled();

        List<MeterStreamLayer> existing = layerRepository.findAllByMeter(meterId);
        for (MeterStreamLayer l : existing) {
            if (l.getLayerType() == req.getLayerType()) {
                throw new BusinessRuleException("This meter already has a " + l.getLayerType() + " layer");
            }
            if (l.getLayerOrder() == order) {
                throw new BusinessRuleException("Position " + order + " is already used by the "
                        + l.getLayerType() + " layer");
            }
        }

        List<LayerSpec> specs = enabledSpecs(existing, null);
        if (enabled) {
            specs.add(new LayerSpec(req.getLayerType(), order, config));
        }
        checkChainBuilds(meter, specs);

        MeterStreamLayer layer = MeterStreamLayer.builder()
                .meter(meter)
                .layerType(req.getLayerType())
                .layerOrder((short) order)
                .config(config)
                .enabled(enabled)
                .build();
        layerRepository.save(layer);
        return layerView(layer);
    }

    public LayerView updateLayer(Long meterId, Long layerId, LayerRequest req) {
        EnergyMeter meter = requireMeter(meterId);
        MeterStreamLayer layer = requireLayer(meterId, layerId);
        if (req.getLayerType() != null && req.getLayerType() != layer.getLayerType()) {
            throw new BusinessRuleException("A layer's type cannot be changed; delete it and add a new one");
        }

        int order = req.getLayerOrder() != null ? checkOrder(req.getLayerOrder()) : layer.getLayerOrder();
        String config = req.getConfig() != null ? cleanConfig(req.getConfig()) : layer.getConfig();
        boolean enabled = req.getEnabled() != null ? req.getEnabled() : layer.getEnabled();

        List<MeterStreamLayer> existing = layerRepository.findAllByMeter(meterId);
        for (MeterStreamLayer other : existing) {
            if (!other.getId().equals(layerId) && other.getLayerOrder() == order) {
                throw new BusinessRuleException("Position " + order + " is already used by the "
                        + other.getLayerType() + " layer");
            }
        }

        List<LayerSpec> specs = enabledSpecs(existing, layerId);
        if (enabled) {
            specs.add(new LayerSpec(layer.getLayerType(), order, config));
        }
        checkChainBuilds(meter, specs);

        layer.setLayerOrder((short) order);
        layer.setConfig(config);
        layer.setEnabled(enabled);
        return layerView(layer);
    }

    public void deleteLayer(Long meterId, Long layerId) {
        layerRepository.delete(requireLayer(meterId, layerId));
    }

    // ---------------------------------------------------------------- helpers

    private EnergyMeter requireMeter(Long meterId) {
        EnergyMeter meter = meterRepository.findById(meterId);
        if (meter == null) {
            throw new ResourceNotFoundException("Meter " + meterId + " not found");
        }
        return meter;
    }

    private MeterStreamLayer requireLayer(Long meterId, Long layerId) {
        MeterStreamLayer layer = layerRepository.findById(layerId);
        if (layer == null || !layer.getMeter().getId().equals(meterId)) {
            throw new ResourceNotFoundException("Layer " + layerId + " not found on meter " + meterId);
        }
        return layer;
    }

    private int checkOrder(Integer order) {
        if (order == null || order < 1 || order > MAX_LAYER_ORDER) {
            throw new BusinessRuleException("Layer position must be a number from 1 to " + MAX_LAYER_ORDER
                    + " (1 is applied first)");
        }
        return order;
    }

    private String cleanConfig(String config) {
        return config == null || config.isBlank() ? null : config.trim();
    }

    private List<LayerSpec> enabledSpecs(List<MeterStreamLayer> layers, Long skipLayerId) {
        List<LayerSpec> specs = new ArrayList<>();
        for (MeterStreamLayer l : layers) {
            if (Boolean.TRUE.equals(l.getEnabled()) && !l.getId().equals(skipLayerId)) {
                specs.add(new LayerSpec(l.getLayerType(), l.getLayerOrder(), l.getConfig()));
            }
        }
        return specs;
    }

    private void checkChainBuilds(EnergyMeter meter, List<LayerSpec> specs) {
        try {
            MeterStreamAssembler.assemble(meter.getMeterCode(), specs);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        }
    }

    private MeterView toView(EnergyMeter meter) {
        List<MeterStreamLayer> layers = layerRepository.findAllByMeter(meter.getId());
        List<LayerView> layerViews = layers.stream().map(this::layerView).toList();
        String chain;
        try {
            chain = MeterStreamAssembler.assemble(meter.getMeterCode(), enabledSpecs(layers, null)).describe();
        } catch (IllegalArgumentException e) {
            chain = "INVALID: " + e.getMessage();
        }
        Zone zone = meter.getZone();
        return new MeterView(meter.getId(), meter.getMeterCode(), zone.getId(), zone.getName(), zone.getCode(),
                meter.getStatus().name(), meter.getInstalledAt(), chain, layerViews);
    }

    private LayerView layerView(MeterStreamLayer l) {
        return new LayerView(l.getId(), l.getLayerType(), l.getLayerOrder(), l.getConfig(), l.getEnabled());
    }
}