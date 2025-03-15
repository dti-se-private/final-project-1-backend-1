package org.dti.se.finalproject1backend1.outers.deliveries.gateways;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Properties;


@Component
public class GmailGateway {

    @Autowired
    Environment environment;

    @Autowired
    JavaMailSender mailSender;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("gmail.smtp.host"));
        mailSender.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("gmail.smtp.port"))));
        mailSender.setUsername(environment.getProperty("gmail.smtp.username"));
        mailSender.setPassword(environment.getProperty("gmail.smtp.password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
