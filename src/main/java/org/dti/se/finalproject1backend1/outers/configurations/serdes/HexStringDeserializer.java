package org.dti.se.finalproject1backend1.outers.configurations.serdes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;

public class HexStringDeserializer extends JsonDeserializer<byte[]> {
    @Override
    public byte[] deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String hexString = parser.getText();
        if (hexString == null) {
            return null;
        }

        if (hexString.startsWith("\\x")) {
            hexString = hexString.substring(2);
        }

        try {
            return Hex.decodeHex(hexString);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }
}
