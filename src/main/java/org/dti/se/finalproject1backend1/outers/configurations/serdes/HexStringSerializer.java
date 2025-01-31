package org.dti.se.finalproject1backend1.outers.configurations.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;

public class HexStringSerializer extends JsonSerializer<byte[]> {
    @Override
    public void serialize(byte[] value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        generator.writeString(Hex.encodeHexString(value));
    }
}
