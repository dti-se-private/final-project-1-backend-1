package org.dti.se.finalproject1backend1.outers.configurations.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBWriter;

import java.io.IOException;

import static org.locationtech.jts.io.WKBWriter.bytesToHex;

public class PointSerializer extends JsonSerializer<Point> {
    @Override
    public void serialize(Point value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        // convert to wkb hex string
        WKBWriter writer = new WKBWriter();
        byte[] wkb = writer.write(value);
        generator.writeString(bytesToHex(wkb));
    }
}