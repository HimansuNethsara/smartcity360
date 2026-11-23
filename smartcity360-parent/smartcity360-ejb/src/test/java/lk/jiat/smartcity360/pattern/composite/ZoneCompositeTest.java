package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ZoneCompositeTest {

    private ZoneComponent city;

    private static ZoneRow row(long id, Long parent, ZoneType type, PowerStatus power) {
        return new ZoneRow(id, parent, type, "Z" + id, "Zone " + id, LandUse.MIXED, power);
    }

    private static List<ZoneRow> sampleRows() {
        return List.of(
                row(1, null, ZoneType.CITY, PowerStatus.ON),
                row(2, 1L, ZoneType.DISTRICT, PowerStatus.ON),
                row(3, 2L, ZoneType.STREET, PowerStatus.ON),
                row(4, 3L, ZoneType.BUILDING, PowerStatus.ON),
                row(5, 3L, ZoneType.BUILDING, PowerStatus.ON),
                row(6, 2L, ZoneType.STREET, PowerStatus.ON),
                row(7, 6L, ZoneType.BUILDING, PowerStatus.ON));
    }

    private static Map<Long, LeafUsage> sampleUsage() {
        return Map.of(
                4L, new LeafUsage(new BigDecimal("10"), 2),
                5L, new LeafUsage(new BigDecimal("5"), 1),
                7L, new LeafUsage(new BigDecimal("20"), 3));
    }

    private static void assertKwh(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " kWh but was " + actual);
    }

    @BeforeEach
    void buildTree() {
        List<ZoneComponent> roots = ZoneTreeBuilder.build(sampleRows(), sampleUsage());
        assertEquals(1, roots.size());
        city = roots.get(0);
    }

    // ------------------------------------------------------------------ building the tree

    @Test
    @DisplayName("The builder turns flat rows into one rooted tree; buildings are leaves")
    void buildsTree() {
        assertEquals(ZoneType.CITY, city.getType());
        assertNull(city.getParent());
        ZoneComponent streetA = city.findById(3L);
        assertEquals(2, streetA.getChildren().size());
        assertSame(city.findById(2L), streetA.getParent());
        assertInstanceOf(ZoneLeaf.class, city.findById(4L));
        assertInstanceOf(ZoneComposite.class, streetA);
        assertTrue(city.findById(4L).getChildren().isEmpty());
    }

    @Test
    @DisplayName("findById finds nodes at any depth and returns null for unknown ids")
    void findById() {
        assertEquals("Z7", city.findById(7L).getCode());
        assertNull(city.findById(99L));
    }

    @Test
    @DisplayName("A building cannot contain other zones")
    void buildingCannotHaveChildren() {
        List<ZoneRow> rows = List.of(
                row(1, null, ZoneType.BUILDING, PowerStatus.ON),
                row(2, 1L, ZoneType.STREET, PowerStatus.ON));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ZoneTreeBuilder.build(rows, Map.of()));
        assertTrue(ex.getMessage().contains("cannot contain zones"));
    }

    @Test
    @DisplayName("A loop in the zone data is detected instead of recursing forever")
    void loopDetected() {
        List<ZoneRow> rows = List.of(
                row(1, 2L, ZoneType.DISTRICT, PowerStatus.ON),
                row(2, 1L, ZoneType.DISTRICT, PowerStatus.ON));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ZoneTreeBuilder.build(rows, Map.of()));
        assertTrue(ex.getMessage().contains("loop"));
    }

    @Test
    @DisplayName("A zone whose parent is not in the data set becomes a root")
    void orphanBecomesRoot() {
        List<ZoneComponent> roots = ZoneTreeBuilder.build(
                List.of(row(1, 99L, ZoneType.DISTRICT, PowerStatus.ON)), Map.of());
        assertEquals(1, roots.size());
    }

    // ------------------------------------------------------------------ uniform operations

    @Test
    @DisplayName("Shutting down a street switches off the street and its buildings, and reports how many changed")
    void shutdownCascades() {
        int changed = city.findById(3L).shutdownPower();
        assertEquals(3, changed);
        assertTrue(city.findById(3L).isOff());
        assertTrue(city.findById(4L).isOff());
        assertTrue(city.findById(5L).isOff());
        assertFalse(city.findById(6L).isOff(), "siblings are untouched");
        assertFalse(city.isOff(), "ancestors are untouched");
    }

    @Test
    @DisplayName("The same call works on a single building (leaf) and on the whole city (composite)")
    void sameOperationOnLeafAndComposite() {
        assertEquals(1, city.findById(7L).shutdownPower());
        assertEquals(6, city.shutdownPower(), "city + district + 2 streets + 2 remaining buildings");
    }

    @Test
    @DisplayName("Shutting down twice changes nothing the second time")
    void shutdownIsIdempotent() {
        city.shutdownPower();
        assertEquals(0, city.shutdownPower());
    }

    @Test
    @DisplayName("A zone cannot be restored while its parent is off")
    void cannotRestoreUnderOffParent() {
        ZoneComponent district = city.findById(2L);
        district.shutdownPower();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> city.findById(3L).restorePower());
        assertTrue(ex.getMessage().contains("restore the parent"));
    }

    @Test
    @DisplayName("Restoring the parent brings the whole subtree back")
    void restoreParentRestoresSubtree() {
        ZoneComponent district = city.findById(2L);
        district.shutdownPower();
        assertEquals(6, district.restorePower());
        assertFalse(city.findById(7L).isOff());
    }

    @Test
    @DisplayName("A child can be restored on its own once the parent is on")
    void restoreChildWhenParentOn() {
        city.findById(3L).shutdownPower();
        assertEquals(3, city.findById(3L).restorePower());
    }

    // ------------------------------------------------------------------ usage report

    @Test
    @DisplayName("Usage is summed up the tree: kWh, meters and buildings")
    void usageRollsUp() {
        UsageReport r = city.reportUsage();
        assertKwh("35", r.kwh());
        assertEquals(6, r.meters());
        assertEquals(3, r.buildings());
        assertEquals(1, r.children().size());

        UsageReport streetA = city.findById(3L).reportUsage();
        assertKwh("15", streetA.kwh());
        assertEquals(3, streetA.meters());
        assertEquals(2, streetA.buildings());
    }

    @Test
    @DisplayName("A zone that is off draws no energy, and the roll-up reflects it")
    void offZoneDrawsNothing() {
        city.findById(3L).shutdownPower();
        assertKwh("0", city.findById(3L).reportUsage().kwh());
        assertKwh("20", city.reportUsage().kwh());
        assertEquals(PowerStatus.OFF, city.findById(4L).reportUsage().power());
    }

    @Test
    @DisplayName("A building with no usage data reports zero")
    void leafWithoutUsage() {
        ZoneComponent lone = ZoneTreeBuilder.build(
                List.of(row(1, null, ZoneType.BUILDING, PowerStatus.ON)), Map.of()).get(0);
        UsageReport r = lone.reportUsage();
        assertKwh("0", r.kwh());
        assertEquals(0, r.meters());
        assertEquals(1, r.buildings());
    }

    @Test
    @DisplayName("The children list cannot be modified from outside")
    void childrenUnmodifiable() {
        List<ZoneComponent> kids = city.getChildren();
        ZoneComponent extra = city.findById(7L);
        assertThrows(UnsupportedOperationException.class, () -> kids.add(extra));
    }

    // ------------------------------------------------------------------ visitors

    @Test
    @DisplayName("ZoneStatsVisitor counts nodes per type, powered-off nodes and tree depth")
    void statsVisitor() {
        city.findById(6L).shutdownPower();
        ZoneStatsVisitor stats = new ZoneStatsVisitor();
        city.accept(stats);
        assertEquals(7, stats.getTotal());
        assertEquals(4, stats.getLevels());
        assertEquals(2, stats.getOff(), "street B and its building");
        assertEquals(1, (int) stats.getPerType().get(ZoneType.CITY));
        assertEquals(2, (int) stats.getPerType().get(ZoneType.STREET));
        assertEquals(3, (int) stats.getPerType().get(ZoneType.BUILDING));
    }

    @Test
    @DisplayName("ChangedZonesVisitor lists only zones whose power differs from how they were loaded")
    void changedVisitor() {
        city.findById(3L).shutdownPower();
        ChangedZonesVisitor changed = new ChangedZonesVisitor();
        city.accept(changed);
        assertEquals(List.of(3L, 4L, 5L), changed.getChanged().stream().map(ZoneComponent::getId).toList());

        city.findById(3L).restorePower();
        ChangedZonesVisitor after = new ChangedZonesVisitor();
        city.accept(after);
        assertTrue(after.getChanged().isEmpty(), "switching back to the original state is not a change");
    }
}
