package lk.jiat.smartcity360.pattern.decorator;

import lk.jiat.smartcity360.domain.enums.LayerType;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler.LayerSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterStreamDecoratorTest {

    private static final String METER = "MTR-001";
    private static final byte[] PAYLOAD =
            "{\"meter\":\"MTR-001\",\"kwh\":12.5,\"ts\":\"2026-11-05T10:00:00\"}".getBytes(StandardCharsets.UTF_8);

    @Mock
    MeterStream inner;

    private static MeterStream stack(String meter, LayerSpec... specs) {
        return MeterStreamAssembler.assemble(meter, List.of(specs));
    }

    private static LayerSpec layer(LayerType t, int order, String cfg) {
        return new LayerSpec(t, order, cfg);
    }

    // ------------------------------------------------------------------ base component

    @Test
    @DisplayName("Raw stream passes the payload through unchanged")
    void rawIsPassThrough() {
        MeterStream raw = new RawMeterStream();
        assertArrayEquals(PAYLOAD, raw.send(PAYLOAD));
        assertArrayEquals(PAYLOAD, raw.receive(PAYLOAD));
        assertEquals("Raw", raw.describe());
    }

    @Test
    @DisplayName("No layers assembles to a raw stream")
    void emptyAssemblyIsRaw() {
        MeterStream s = MeterStreamAssembler.assemble(METER, List.of());
        assertEquals("Raw", s.describe());
        assertArrayEquals(PAYLOAD, s.send(PAYLOAD));
    }

    // ------------------------------------------------------------------ decorator delegation (mock)

    @Test
    @DisplayName("A decorator with no behaviour of its own delegates to the wrapped stream")
    void baseDecoratorDelegates() {
        MeterStreamDecorator passthrough = new MeterStreamDecorator(inner) { };
        when(inner.send(PAYLOAD)).thenReturn(new byte[]{1});
        when(inner.receive(new byte[]{2})).thenReturn(new byte[]{3});
        when(inner.describe()).thenReturn("Inner");

        assertArrayEquals(new byte[]{1}, passthrough.send(PAYLOAD));
        assertArrayEquals(new byte[]{3}, passthrough.receive(new byte[]{2}));
        assertEquals("Inner", passthrough.describe());
        verify(inner).send(PAYLOAD);
    }

    @Test
    @DisplayName("A decorator refuses a null stream to wrap")
    void nullWrappedRejected() {
        assertThrows(NullPointerException.class, () -> new MeterStreamDecorator(null) { });
    }

    // ------------------------------------------------------------------ single layers

    @Test
    @DisplayName("Compression round-trips and shrinks repetitive data")
    void compressionRoundTrip() {
        MeterStream s = stack(METER, layer(LayerType.COMPRESSION, 1, "GZIP"));
        byte[] big = "AAAAAAAAAA".repeat(500).getBytes(StandardCharsets.UTF_8);
        byte[] wire = s.send(big);
        assertTrue(wire.length < big.length / 10, "gzip should shrink repetitive data a lot");
        assertArrayEquals(big, s.receive(wire));
    }

    @Test
    @DisplayName("Compression rejects data that is not gzip")
    void compressionRejectsGarbage() {
        MeterStream s = stack(METER, layer(LayerType.COMPRESSION, 1, null));
        assertThrows(StreamProcessingException.class, () -> s.receive(new byte[]{1, 2, 3, 4}));
    }

    @Test
    @DisplayName("Encryption round-trips, hides the plaintext and uses a fresh IV each time")
    void encryptionRoundTrip() {
        MeterStream s = stack(METER, layer(LayerType.ENCRYPTION, 1, "AES-256"));
        byte[] w1 = s.send(PAYLOAD);
        byte[] w2 = s.send(PAYLOAD);
        assertFalse(Arrays.equals(PAYLOAD, w1));
        assertFalse(Arrays.equals(w1, w2), "random IV means the same payload encrypts differently");
        assertArrayEquals(PAYLOAD, s.receive(w1));
        assertArrayEquals(PAYLOAD, s.receive(w2));
    }

    @Test
    @DisplayName("Encryption detects tampering and rejects short messages")
    void encryptionDetectsTamperingAndShortInput() {
        MeterStream s = stack(METER, layer(LayerType.ENCRYPTION, 1, null));
        byte[] wire = s.send(PAYLOAD);
        wire[wire.length - 1] ^= 0x01;
        assertThrows(StreamProcessingException.class, () -> s.receive(wire));
        assertThrows(StreamProcessingException.class, () -> s.receive(new byte[5]));
        assertThrows(StreamProcessingException.class, () -> s.receive(null));
    }

    @Test
    @DisplayName("Authentication appends a 32-byte tag and verifies it")
    void authenticationRoundTrip() {
        MeterStream s = stack(METER, layer(LayerType.AUTHENTICATION, 1, "TOKEN"));
        byte[] wire = s.send(PAYLOAD);
        assertEquals(PAYLOAD.length + 32, wire.length);
        assertArrayEquals(PAYLOAD, s.receive(wire));
    }

    @Test
    @DisplayName("Authentication rejects altered data and short messages")
    void authenticationDetectsTampering() {
        MeterStream s = stack(METER, layer(LayerType.AUTHENTICATION, 1, null));
        byte[] wire = s.send(PAYLOAD);
        wire[0] ^= 0x01;
        StreamProcessingException ex = assertThrows(StreamProcessingException.class, () -> s.receive(wire));
        assertTrue(ex.getMessage().contains("Authentication failed"));
        assertThrows(StreamProcessingException.class, () -> s.receive(new byte[10]));
    }

    @Test
    @DisplayName("A message signed for one meter is rejected by another meter's stream")
    void authenticationIsPerMeter() {
        MeterStream a = stack("MTR-A", layer(LayerType.AUTHENTICATION, 1, null));
        MeterStream b = stack("MTR-B", layer(LayerType.AUTHENTICATION, 1, null));
        byte[] wire = a.send(PAYLOAD);
        assertThrows(StreamProcessingException.class, () -> b.receive(wire));
    }

    // ------------------------------------------------------------------ stacking and order

    @Test
    @DisplayName("Layers are applied in 'order' regardless of list order, and describe() shows the stack")
    void assemblerSortsByOrder() {
        MeterStream s = MeterStreamAssembler.assemble(METER, List.of(
                layer(LayerType.AUTHENTICATION, 3, null),
                layer(LayerType.COMPRESSION, 1, null),
                layer(LayerType.ENCRYPTION, 2, null)));
        assertEquals("Raw>Compress>Encrypt>Auth", s.describe());
    }

    @Test
    @DisplayName("A full Compression > Encryption > Authentication stack round-trips")
    void fullStackRoundTrip() {
        MeterStream s = stack(METER,
                layer(LayerType.COMPRESSION, 1, "GZIP"),
                layer(LayerType.ENCRYPTION, 2, "AES-128"),
                layer(LayerType.AUTHENTICATION, 3, "TOKEN"));
        assertArrayEquals(PAYLOAD, s.receive(s.send(PAYLOAD)));
    }

    @Test
    @DisplayName("Stack order matters: a differently ordered stack cannot read the message")
    void orderMatters() {
        MeterStream sender = stack(METER,
                layer(LayerType.COMPRESSION, 1, null), layer(LayerType.ENCRYPTION, 2, null));
        MeterStream receiver = stack(METER,
                layer(LayerType.ENCRYPTION, 1, null), layer(LayerType.COMPRESSION, 2, null));
        byte[] wire = sender.send(PAYLOAD);
        assertThrows(StreamProcessingException.class, () -> receiver.receive(wire));
    }

    // ------------------------------------------------------------------ configuration errors

    @Test
    @DisplayName("Unsupported layer configs are rejected when the stack is built")
    void badConfigRejected() {
        assertThrows(IllegalArgumentException.class, () -> stack(METER, layer(LayerType.COMPRESSION, 1, "ZIP")));
        assertThrows(IllegalArgumentException.class, () -> stack(METER, layer(LayerType.ENCRYPTION, 1, "DES")));
        assertThrows(IllegalArgumentException.class, () -> stack(METER, layer(LayerType.AUTHENTICATION, 1, "OAUTH")));
    }
}
