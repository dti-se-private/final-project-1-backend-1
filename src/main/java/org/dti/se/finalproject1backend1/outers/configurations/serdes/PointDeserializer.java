package org.dti.se.finalproject1backend1.outers.configurations.serdes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import java.io.IOException;

public class PointDeserializer extends JsonDeserializer<Point> {
    @Override
    public Point deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String text = parser.getText();
        if (text == null) {
            return null;
        }

        WKBReader reader = new WKBReader();
        try {
            return (Point) reader.read(WKBReader.hexToBytes(text));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
