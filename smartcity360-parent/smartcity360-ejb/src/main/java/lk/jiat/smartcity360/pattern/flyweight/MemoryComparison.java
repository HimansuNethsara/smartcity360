package lk.jiat.smartcity360.pattern.flyweight;

import lk.jiat.smartcity360.domain.enums.StreetlightStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class MemoryComparison {

    public static final int MAX_LIGHTS = 200_000;

    private MemoryComparison() {
    }

    public static MemoryReport compare(List<LightTypeSpec> specs, int lights) {
        if (specs.isEmpty()) {
            throw new IllegalArgumentException("At least one light type is needed");
        }
        if (lights < 1 || lights > MAX_LIGHTS) {
            throw new IllegalArgumentException("Light count must be between 1 and " + MAX_LIGHTS);
        }

        LightTypeFactory factory = new LightTypeFactory();

        long before = usedHeap();
        List<StreetLight> shared = new ArrayList<>(lights);
        fill(lights, specs, (i, spec, r) ->
                shared.add(new StreetLight(i, 1 + i % 50, lat(r), lon(r), factory.get(spec), status(r), brightness(r))));
        long flyweightMeasured = usedHeap() - before;

        before = usedHeap();
        List<NaiveStreetLight> naive = new ArrayList<>(lights);
        fill(lights, specs, (i, spec, r) ->
                naive.add(new NaiveStreetLight(i, 1 + i % 50, lat(r), lon(r), status(r), brightness(r), spec)));
        long naiveMeasured = usedHeap() - before;

        Set<LightType> distinct = Collections.newSetFromMap(new IdentityHashMap<>());
        for (StreetLight s : shared) {
            distinct.add(s.getType());
        }

        long typeBytes = 0;
        for (LightType t : factory.pooledTypes()) {
            typeBytes += typeObjectBytes(t);
        }
        long naiveEstimate = 0;
        for (NaiveStreetLight n : naive) {
            naiveEstimate += naiveObjectBytes(n);
        }
        long flyweightEstimate = (long) lights * STREETLIGHT_BYTES + typeBytes;
        double saved = naiveEstimate == 0 ? 0 : Math.round((1.0 - (double) flyweightEstimate / naiveEstimate) * 1000) / 10.0;

        return new MemoryReport(lights, specs.size(), distinct.size(), factory.getRequests(), factory.getHits(),
                naiveEstimate, flyweightEstimate, saved, Math.max(naiveMeasured, 0), Math.max(flyweightMeasured, 0));
    }

    // ---------------------------------------------------------------- size model (64-bit, compressed refs)

    private static final int HEADER = 12;
    private static final int REF = 4;
    /** header + id,zoneId,lat,lon (4 x 8) + status ref + brightness short + type ref */
    static final int STREETLIGHT_BYTES = align(HEADER + 8 + 8 + 8 + 8 + REF + 2 + REF);

    private static int align(int bytes) {
        return (bytes + 7) / 8 * 8;
    }

    private static long stringBytes(String s) {
        int len = s.getBytes(StandardCharsets.UTF_8).length;
        return align(HEADER + 4 + 1 + 1 + REF) + align(16 + len);   // String object + its byte[]
    }

    private static long typeObjectBytes(LightType t) {
        // header + id,wattage,maxLumens,colorTempK (4 x 4) + 3 string refs + lampType ref
        long shell = align(HEADER + 16 + 3 * REF + REF);
        return shell + stringBytes(t.getTypeCode()) + stringBytes(t.getModel()) + stringBytes(t.getFirmware());
    }

    private static long naiveObjectBytes(NaiveStreetLight n) {
        long shell = align(HEADER + 8 + 8 + 8 + 8 + REF + 2 + 16 + 3 * REF + REF);
        return shell + stringBytes(n.typeCode) + stringBytes(n.model) + stringBytes(n.firmware);
    }

    // ---------------------------------------------------------------- generation

    private interface Maker {
        void make(int i, LightTypeSpec spec, Random r);
    }

    private static void fill(int lights, List<LightTypeSpec> specs, Maker maker) {
        Random r = new Random(42);   // same seed: both designs see identical data
        for (int i = 1; i <= lights; i++) {
            maker.make(i, specs.get(i % specs.size()), r);
        }
    }

    private static double lat(Random r) { return 6.80 + r.nextDouble() * 0.2; }
    private static double lon(Random r) { return 79.80 + r.nextDouble() * 0.2; }
    private static int brightness(Random r) { return 40 + r.nextInt(61); }
    private static StreetlightStatus status(Random r) {
        int x = r.nextInt(100);
        return x < 90 ? StreetlightStatus.ON : x < 96 ? StreetlightStatus.OFF : StreetlightStatus.FAULTY;
    }

    private static long usedHeap() {
        Runtime rt = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            System.gc();
        }
        return rt.totalMemory() - rt.freeMemory();
    }
}
