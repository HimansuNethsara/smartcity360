package lk.jiat.smartcity360.arquillian;

import lk.jiat.smartcity360.domain.enums.LampType;
import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import lk.jiat.smartcity360.pattern.composite.LeafUsage;
import lk.jiat.smartcity360.pattern.composite.ZoneComponent;
import lk.jiat.smartcity360.pattern.composite.ZoneRow;
import lk.jiat.smartcity360.pattern.composite.ZoneTreeBuilder;
import lk.jiat.smartcity360.pattern.flyweight.LightType;
import lk.jiat.smartcity360.pattern.flyweight.LightTypeFactory;
import lk.jiat.smartcity360.pattern.flyweight.LightTypeSpec;
import lk.jiat.smartcity360.pattern.flyweight.MemoryComparison;
import lk.jiat.smartcity360.pattern.flyweight.MemoryReport;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("arquillian")
@ExtendWith(ArquillianExtension.class)
public class CompositeFlyweightArquillianIT {

    @Deployment
    public static WebArchive deployment() {
        return Deployments.war("sc360-composite-flyweight-it",
                "lk.jiat.smartcity360.pattern.composite", "lk.jiat.smartcity360.pattern.flyweight",
                "lk.jiat.smartcity360.domain.enums");
    }

    private static ZoneRow row(long id, Long parent, ZoneType type) {
        return new ZoneRow(id, parent, type, "Z" + id, "Zone " + id, LandUse.MIXED, PowerStatus.ON);
    }

    private static ZoneComponent city() {
        List<ZoneRow> rows = List.of(
                row(1, null, ZoneType.CITY), row(2, 1L, ZoneType.DISTRICT), row(3, 2L, ZoneType.STREET),
                row(4, 3L, ZoneType.BUILDING), row(5, 3L, ZoneType.BUILDING));
        Map<Long, LeafUsage> usage = Map.of(
                4L, new LeafUsage(new BigDecimal("10"), 2), 5L, new LeafUsage(new BigDecimal("5"), 1));
        return ZoneTreeBuilder.build(rows, usage).get(0);
    }

    @Test
    @DisplayName("Usage rolls up the zone tree")
    void compositeUsage() {
        assertEquals(0, new BigDecimal("15").compareTo(city().reportUsage().kwh()));
        assertEquals(3, city().reportUsage().meters());
    }

    @Test
    @DisplayName("Shutting down a street switches off its buildings and the report reflects it")
    void compositeShutdown() {
        ZoneComponent city = city();
        assertEquals(3, city.findById(3L).shutdownPower());
        assertTrue(city.findById(4L).isOff());
        assertEquals(0, BigDecimal.ZERO.compareTo(city.reportUsage().kwh()));
        assertThrows(IllegalStateException.class, () -> {
            city.shutdownPower();
            city.findById(3L).restorePower();
        });
    }

    @Test
    @DisplayName("Flyweight: 4,000 concurrent requests inside the server still create one shared LightType")
    void flyweightIsThreadSafe() throws Exception {
        LightTypeSpec led = new LightTypeSpec(1, "LED-100", "Philips", LampType.LED, 100, 12000, 4000, "1.0");
        LightTypeFactory factory = new LightTypeFactory();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<LightType>> results = new ArrayList<>();
        for (int i = 0; i < 4000; i++) {
            results.add(pool.submit(() -> factory.get(led)));
        }
        LightType first = results.get(0).get();
        for (Future<LightType> f : results) {
            assertSame(first, f.get());
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, factory.getCreated());
        assertEquals(50, first.powerWatts(StreetlightStatus.ON, 50));
    }

    @Test
    @DisplayName("Flyweight uses far less memory than one full object per light")
    void flyweightMemory() {
        LightTypeSpec led = new LightTypeSpec(1, "LED-100", "Philips", LampType.LED, 100, 12000, 4000, "1.0");
        LightTypeSpec na = new LightTypeSpec(2, "NA-70", "GE", LampType.SODIUM, 70, 6600, 2100, "0.9");
        MemoryReport report = MemoryComparison.compare(List.of(led, na), 1000);
        assertEquals(2, report.typeObjectsInFlyweight());
        assertTrue(report.estimatedSavedPercent() > 50.0);
    }
}
