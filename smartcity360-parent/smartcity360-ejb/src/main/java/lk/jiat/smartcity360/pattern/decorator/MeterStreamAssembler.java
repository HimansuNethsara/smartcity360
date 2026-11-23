package lk.jiat.smartcity360.pattern.decorator;

import lk.jiat.smartcity360.domain.enums.LayerType;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MeterStreamAssembler {

    public record LayerSpec(LayerType type, int order, String config) {
    }

    @FunctionalInterface
    private interface DecoratorFactory {
        MeterStreamDecorator create(MeterStream inner, String meterCode, String config);
    }

    private static final Map<LayerType, DecoratorFactory> FACTORIES = new EnumMap<>(LayerType.class);

    static {
        FACTORIES.put(LayerType.AUTHENTICATION, AuthenticationDecorator::new);
        FACTORIES.put(LayerType.COMPRESSION, CompressionDecorator::new);
        FACTORIES.put(LayerType.ENCRYPTION, EncryptionDecorator::new);
    }

    private MeterStreamAssembler() {
    }

    public static MeterStream assemble(String meterCode, List<LayerSpec> layers) {
        MeterStream stream = new RawMeterStream();
        List<LayerSpec> ordered = layers.stream()
                .sorted(Comparator.comparingInt(LayerSpec::order))
                .toList();
        for (LayerSpec spec : ordered) {
            DecoratorFactory factory = FACTORIES.get(spec.type());
            if (factory == null) {
                throw new IllegalStateException("No decorator registered for layer type " + spec.type());
            }
            stream = factory.create(stream, meterCode, spec.config());
        }
        return stream;
    }
}

