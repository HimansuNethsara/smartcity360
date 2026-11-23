package lk.jiat.smartcity360.service;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import lk.jiat.smartcity360.domain.StreetlightType;
import lk.jiat.smartcity360.dto.PoolStats;
import lk.jiat.smartcity360.pattern.flyweight.LightType;
import lk.jiat.smartcity360.pattern.flyweight.LightTypeFactory;
import lk.jiat.smartcity360.pattern.flyweight.LightTypeSpec;


@Singleton
@Lock(LockType.READ)
public class FlyweightRegistry {

    private final LightTypeFactory factory = new LightTypeFactory();

    public LightType typeFor(StreetlightType t) {
        return factory.get(new LightTypeSpec(t.getId(), t.getTypeCode(), t.getModel(), t.getLampType(),
                t.getWattage(), t.getMaxLumens(), t.getColorTempK(), t.getFirmware()));
    }

    public PoolStats stats() {
        return new PoolStats(factory.poolSize(), factory.getRequests(), factory.getHits(), factory.getCreated());
    }

    public void reset() {
        factory.clear();
    }
}
