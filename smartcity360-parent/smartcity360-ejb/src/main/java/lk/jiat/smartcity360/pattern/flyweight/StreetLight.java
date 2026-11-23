package lk.jiat.smartcity360.pattern.flyweight;

import lk.jiat.smartcity360.domain.enums.StreetlightStatus;

public class StreetLight {

    private final long id;
    private final long zoneId;
    private final double latitude;
    private final double longitude;
    private final LightType type;
    private StreetlightStatus status;
    private short brightness;

    public StreetLight(long id, long zoneId, double latitude, double longitude,
                       LightType type, StreetlightStatus status, int brightness) {
        this.id = id;
        this.zoneId = zoneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.status = status;
        this.brightness = (short) brightness;
    }

    public int powerWatts() {
        return type.powerWatts(status, brightness);
    }

    public int lumens() {
        return type.lumens(status, brightness);
    }

    public long getId() { return id; }
    public long getZoneId() { return zoneId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public LightType getType() { return type; }
    public StreetlightStatus getStatus() { return status; }
    public int getBrightness() { return brightness; }
}
