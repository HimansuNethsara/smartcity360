package lk.jiat.smartcity360.pattern.flyweight;

import lk.jiat.smartcity360.domain.enums.LampType;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;

final class NaiveStreetLight {

    final long id;
    final long zoneId;
    final double latitude;
    final double longitude;
    final StreetlightStatus status;
    final short brightness;
    final int typeId;
    final String typeCode;
    final String model;
    final LampType lampType;
    final int wattage;
    final int maxLumens;
    final int colorTempK;
    final String firmware;

    NaiveStreetLight(long id, long zoneId, double latitude, double longitude,
                     StreetlightStatus status, int brightness, LightTypeSpec t) {
        this.id = id;
        this.zoneId = zoneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.brightness = (short) brightness;
        this.typeId = t.id();
        this.typeCode = new String(t.typeCode());   // a fresh copy per light, as a naive row mapper would make
        this.model = new String(t.model());
        this.lampType = t.lampType();
        this.wattage = t.wattage();
        this.maxLumens = t.maxLumens();
        this.colorTempK = t.colorTempK();
        this.firmware = new String(t.firmware());
    }
}
