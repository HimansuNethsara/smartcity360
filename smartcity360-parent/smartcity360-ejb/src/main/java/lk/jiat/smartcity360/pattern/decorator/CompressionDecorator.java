package lk.jiat.smartcity360.pattern.decorator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionDecorator extends MeterStreamDecorator {

    public CompressionDecorator(MeterStream wrapped, String meterCode, String config) {
        super(wrapped);
        if (config != null && !config.isBlank() && !"GZIP".equalsIgnoreCase(config)) {
            throw new IllegalArgumentException("Unsupported compression config '" + config + "' (expected GZIP)");
        }
    }

    @Override
    public byte[] send(byte[] payload) {
        byte[] data = super.send(payload);
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(data);
            gzip.finish();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new StreamProcessingException("Compression failed", e);
        }
    }

    @Override
    public byte[] receive(byte[] wire) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(wire))) {
            return super.receive(gzip.readAllBytes());
        } catch (IOException e) {
            throw new StreamProcessingException("Decompression failed: data is not valid gzip", e);
        }
    }

    @Override
    public String describe() {
        return super.describe() + ">Compress";
    }
}
