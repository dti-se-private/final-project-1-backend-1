package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class MailgunUseCase {
    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    public void sendEmail(String to, String subject, String text) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.mailgun.net/v3/" + domain + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("api", apiKey);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", "Mailgun Sandbox <postmaster@" + domain + ">");
        map.add("to", to);
        map.add("subject", subject);
        map.add("text", text);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        restTemplate.postForObject(url, request, String.class);
    }
}