package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.EnergyMeter;
import lk.jiat.smartcity360.domain.MeterReading;
import lk.jiat.smartcity360.domain.MeterStreamLayer;
import lk.jiat.smartcity360.domain.StreamAudit;
import lk.jiat.smartcity360.domain.enums.MeterStatus;
import lk.jiat.smartcity360.dto.StreamAuditView;
import lk.jiat.smartcity360.dto.StreamResult;
import lk.jiat.smartcity360.pattern.decorator.MeterStream;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler.LayerSpec;
import lk.jiat.smartcity360.pattern.decorator.StreamProcessingException;
import lk.jiat.smartcity360.repository.EnergyMeterRepository;
import lk.jiat.smartcity360.repository.MeterReadingRepository;
import lk.jiat.smartcity360.repository.MeterStreamLayerRepository;
import lk.jiat.smartcity360.repository.StreamAuditRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Stateless
public class MeterStreamService {

    private static final int BATCH_SIZE = 24;
    private static final int AUDIT_TEXT_MAX = 120;

    @EJB
    private EnergyMeterRepository meterRepository;
    @EJB
    private MeterStreamLayerRepository layerRepository;
    @EJB
    private MeterReadingRepository readingRepository;
    @EJB
    private StreamAuditRepository auditRepository;

    public StreamResult transmit(Long meterId) {
        EnergyMeter meter = requireMeter(meterId);
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Meter " + meter.getMeterCode() + " is " + meter.getStatus()
                    + " and cannot transmit");
        }

        List<MeterStreamLayer> layers = layerRepository.findByMeterOrdered(meterId); // enabled only, in order
        List<LayerSpec> specs = layers.stream()
                .map(l -> new LayerSpec(l.getLayerType(), l.getLayerOrder(), l.getConfig()))
                .toList();

        MeterStream stream;
        try {
            stream = MeterStreamAssembler.assemble(meter.getMeterCode(), specs);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Layer configuration is invalid: " + e.getMessage());
        }

        byte[] payload = buildPayload(meter, readingRepository.findRecent(meterId, BATCH_SIZE));
        byte[] wire;
        boolean roundTripOk;
        try {
            wire = stream.send(payload);
            roundTripOk = Arrays.equals(stream.receive(wire), payload);
        } catch (StreamProcessingException e) {
            throw new BusinessRuleException("Stream processing failed: " + e.getMessage());
        }

        String chain = stream.describe();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        StreamAudit audit = StreamAudit.builder()
                .meter(meter)
                .layersApplied(chain.length() > AUDIT_TEXT_MAX ? chain.substring(0, AUDIT_TEXT_MAX) : chain)
                .rawBytes(payload.length)
                .finalBytes(wire.length)
                .processedAt(now)
                .build();
        auditRepository.save(audit);

        return StreamResult.builder()
                .meterCode(meter.getMeterCode())
                .chain(chain)
                .layerCount(specs.size())
                .rawBytes(payload.length)
                .finalBytes(wire.length)
                .sizePercent(Math.round(wire.length * 1000.0 / payload.length) / 10.0)
                .roundTripOk(roundTripOk)
                .wirePreviewHex(hex(wire, 32))
                .auditId(audit.getId())
                .processedAt(now.toString())
                .build();
    }

    public List<StreamAuditView> audit(Long meterId, int limit) {
        requireMeter(meterId);
        int max = Math.min(Math.max(limit, 1), 100);
        return auditRepository.findRecent(meterId, max).stream()
                .map(a -> new StreamAuditView(a.getId(), a.getLayersApplied(), a.getRawBytes(),
                        a.getFinalBytes(), a.getProcessedAt().toString()))
                .toList();
    }

    // ---------------------------------------------------------------- helpers

    private EnergyMeter requireMeter(Long meterId) {
        EnergyMeter meter = meterRepository.findById(meterId);
        if (meter == null) {
            throw new ResourceNotFoundException("Meter " + meterId + " not found");
        }
        return meter;
    }

    private byte[] buildPayload(EnergyMeter meter, List<MeterReading> newestFirst) {
        List<MeterReading> readings = new ArrayList<>(newestFirst);
        Collections.reverse(readings);
        StringBuilder sb = new StringBuilder("{\"meter\":\"").append(meter.getMeterCode()).append("\",\"readings\":[");
        for (int i = 0; i < readings.size(); i++) {
            MeterReading r = readings.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"kwh\":").append(r.getKwh().toPlainString())
                    .append(",\"voltage\":").append(r.getVoltage() == null ? "null" : r.getVoltage().toPlainString())
                    .append(",\"readAt\":\"").append(r.getReadAt()).append("\"}");
        }
        return sb.append("]}").toString().getBytes(StandardCharsets.UTF_8);
    }

    private String hex(byte[] bytes, int maxBytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, maxBytes); i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}