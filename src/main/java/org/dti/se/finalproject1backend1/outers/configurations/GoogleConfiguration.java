package org.dti.se.finalproject1backend1.outers.configurations;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Objects;

@Configuration
public class GoogleConfiguration {

    @Autowired
    Environment environment;


    public GoogleTokenResponse getToken(String authorizationCode) throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new GsonFactory();
        String clientId = environment.getProperty("google.client.id");
        String clientSecret = environment.getProperty("google.client.secret");
        String redirectUri = environment.getProperty("google.client.redirect.uri");
        GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                Objects.requireNonNull(clientId),
                clientSecret,
                authorizationCode,
                redirectUri
        );

        return tokenRequest.execute();
    }

    public static byte[] convertUrlToHexByte(String url) {
        try (InputStream input = new URI(url).toURL().openStream()) {
            String hexString = Hex.encodeHexString(input.readAllBytes());
            return Hex.decodeHex(hexString);
        } catch (IOException | URISyntaxException | DecoderException e) {
            throw new RuntimeException(e);
        }
    }
}
