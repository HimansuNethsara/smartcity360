package lk.jiat.smartcity360.pattern.flyweight;

import lk.jiat.smartcity360.domain.enums.LampType;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;

public final class LightType {

    private final int id;
    private final String typeCode;
    private final String model;
    private final LampType lampType;
    private final int wattage;
    private final int maxLumens;
    private final int colorTempK;
    private final String firmware;

    LightType(LightTypeSpec spec) {
        this.id = spec.id();
        this.typeCode = spec.typeCode();
        this.model = spec.model();
        this.lampType = spec.lampType();
        this.wattage = spec.wattage();
        this.maxLumens = spec.maxLumens();
        this.colorTempK = spec.colorTempK();
        this.firmware = spec.firmware();
    }

    public int powerWatts(StreetlightStatus status, int brightness) {
        return status == StreetlightStatus.ON ? (int) Math.round(wattage * brightness / 100.0) : 0;
    }

    public int lumens(StreetlightStatus status, int brightness) {
        return status == StreetlightStatus.ON ? (int) Math.round(maxLumens * brightness / 100.0) : 0;
    }

    public int getId() { return id; }
    public String getTypeCode() { return typeCode; }
    public String getModel() { return model; }
    public LampType getLampType() { return lampType; }
    public int getWattage() { return wattage; }
    public int getMaxLumens() { return maxLumens; }
    public int getColorTempK() { return colorTempK; }
    public String getFirmware() { return firmware; }
}
