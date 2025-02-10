package org.dti.se.finalproject1backend1.outers.deliveries.gateways;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class MailgunGateway {

    @Autowired
    Environment environment;


    public void sendEmail(String to, String subject, String text) {
        RestTemplate restTemplate = new RestTemplate();
        String url = environment.getProperty("mailgun.api.url") + environment.getProperty("mailgun.domain") + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("api", environment.getProperty("mailgun.api.key"));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", "Mailgun Sandbox <postmaster@" + environment.getProperty("mailgun.domain") + ">");
        map.add("to", to);
        map.add("subject", subject);
        map.add("text", text);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        restTemplate.postForObject(url, request, String.class);
    }
}