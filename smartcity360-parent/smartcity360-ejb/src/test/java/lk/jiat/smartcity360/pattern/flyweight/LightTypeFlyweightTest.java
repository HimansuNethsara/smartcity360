package lk.jiat.smartcity360.pattern.flyweight;

import lk.jiat.smartcity360.domain.enums.LampType;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LightTypeFlyweightTest {

    private static final LightTypeSpec LED = new LightTypeSpec(1, "LED-100", "Philips RoadFlair", LampType.LED, 100, 12000, 4000, "1.2.0");
    private static final LightTypeSpec SODIUM = new LightTypeSpec(2, "NA-70", "GE Lucalox", LampType.SODIUM, 70, 6600, 2100, "0.9.1");

    // ------------------------------------------------------------------ factory / sharing

    @Test
    @DisplayName("The same type id always returns the same shared instance")
    void sameIdSameInstance() {
        LightTypeFactory f = new LightTypeFactory();
        LightType a = f.get(LED);
        LightType b = f.get(LED);
        assertSame(a, b);
        assertEquals(1, f.poolSize());
    }

    @Test
    @DisplayName("Different type ids get different instances")
    void differentIdDifferentInstance() {
        LightTypeFactory f = new LightTypeFactory();
        assertNotSame(f.get(LED), f.get(SODIUM));
        assertEquals(2, f.poolSize());
    }

    @Test
    @DisplayName("Requests, creations and hits are counted")
    void statistics() {
        LightTypeFactory f = new LightTypeFactory();
        for (int i = 0; i < 10; i++) {
            f.get(i % 2 == 0 ? LED : SODIUM);
        }
        assertEquals(10, f.getRequests());
        assertEquals(2, f.getCreated());
        assertEquals(8, f.getHits());
    }

    @Test
    @DisplayName("evict drops one type so the next request creates a new instance")
    void evictRecreates() {
        LightTypeFactory f = new LightTypeFactory();
        LightType first = f.get(LED);
        f.evict(LED.id());
        assertEquals(0, f.poolSize());
        assertNotSame(first, f.get(LED));
    }

    @Test
    @DisplayName("clear empties the pool and resets the counters")
    void clearResets() {
        LightTypeFactory f = new LightTypeFactory();
        f.get(LED);
        f.get(SODIUM);
        f.clear();
        assertEquals(0, f.poolSize());
        assertEquals(0, f.getRequests());
        assertEquals(0, f.getCreated());
        assertTrue(f.pooledTypes().isEmpty());
    }

    @Test
    @DisplayName("The pool stays correct under concurrent use: one instance, every request counted")
    void threadSafe() throws Exception {
        LightTypeFactory f = new LightTypeFactory();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<LightType>> results = new ArrayList<>();
        for (int i = 0; i < 8000; i++) {
            results.add(pool.submit(() -> f.get(LED)));
        }
        LightType first = results.get(0).get();
        for (Future<LightType> r : results) {
            assertSame(first, r.get());
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, f.getCreated());
        assertEquals(8000, f.getRequests());
    }

    // ------------------------------------------------------------------ intrinsic data and calculations

    @Test
    @DisplayName("A LightType exposes its intrinsic data")
    void intrinsicData() {
        LightType t = new LightTypeFactory().get(LED);
        assertEquals("LED-100", t.getTypeCode());
        assertEquals(LampType.LED, t.getLampType());
        assertEquals(100, t.getWattage());
        assertEquals(12000, t.getMaxLumens());
        assertEquals(4000, t.getColorTempK());
        assertEquals("1.2.0", t.getFirmware());
    }

    @Test
    @DisplayName("Power and lumens scale with brightness when the light is ON")
    void scalesWhenOn() {
        LightType t = new LightTypeFactory().get(LED);
        assertEquals(100, t.powerWatts(StreetlightStatus.ON, 100));
        assertEquals(50, t.powerWatts(StreetlightStatus.ON, 50));
        assertEquals(6000, t.lumens(StreetlightStatus.ON, 50));
    }

    @Test
    @DisplayName("Power is rounded to the nearest watt")
    void rounding() {
        LightType t = new LightTypeFactory().get(SODIUM);   // 70 W * 33% = 23.1
        assertEquals(23, t.powerWatts(StreetlightStatus.ON, 33));
    }

    @ParameterizedTest
    @EnumSource(value = StreetlightStatus.class, names = {"OFF", "FAULTY"})
    @DisplayName("A light that is OFF or FAULTY draws no power and gives no light")
    void noPowerWhenNotOn(StreetlightStatus status) {
        LightType t = new LightTypeFactory().get(LED);
        assertEquals(0, t.powerWatts(status, 100));
        assertEquals(0, t.lumens(status, 100));
    }

    // ------------------------------------------------------------------ the flyweight users

    @Test
    @DisplayName("Many street lights share one LightType; only their own state differs")
    void lightsShareType() {
        LightTypeFactory f = new LightTypeFactory();
        StreetLight a = new StreetLight(1, 10, 6.9, 79.8, f.get(LED), StreetlightStatus.ON, 80);
        StreetLight b = new StreetLight(2, 11, 6.8, 79.9, f.get(LED), StreetlightStatus.OFF, 40);
        assertSame(a.getType(), b.getType());
        assertEquals(80, a.powerWatts());
        assertEquals(0, b.powerWatts());
        assertEquals(9600, a.lumens());
        assertEquals(1, f.poolSize());
    }

    // ------------------------------------------------------------------ memory comparison

    @Test
    @DisplayName("Memory comparison reports that the flyweight design is smaller")
    void memoryComparison() {
        MemoryReport r = MemoryComparison.compare(List.of(LED, SODIUM), 1000);
        assertEquals(1000, r.lights());
        assertEquals(2, r.distinctTypes());
        assertEquals(2, r.typeObjectsInFlyweight());
        assertEquals(1000, r.requests());
        assertEquals(998, r.hits());
        assertTrue(r.flyweightEstimatedBytes() < r.naiveEstimatedBytes());
        assertTrue(r.estimatedSavedPercent() > 50.0, "expected a large saving, got " + r.estimatedSavedPercent());
    }

    @Test
    @DisplayName("Memory comparison validates its inputs")
    void memoryComparisonValidation() {
        assertThrows(IllegalArgumentException.class, () -> MemoryComparison.compare(List.of(), 100));
        assertThrows(IllegalArgumentException.class, () -> MemoryComparison.compare(List.of(LED), 0));
        assertThrows(IllegalArgumentException.class,
                () -> MemoryComparison.compare(List.of(LED), MemoryComparison.MAX_LIGHTS + 1));
    }
}
