package org.dti.se.finalproject1backend1;

import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinalProject1Backend1Application implements CommandLineRunner {
    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Override
    public void run(String... args) {
        String rawPassword = "password";
        String encodedPassword = securityConfiguration.encode(rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
    }

    public static void main(String[] args) {
        SpringApplication.run(FinalProject1Backend1Application.class, args);
    }
}
