package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class OtpUseCase {

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private MailgunUseCase mailgunUseCase;

    public void sendOtp(String email, String type) {
        String otp = generateOtp();
        OffsetDateTime initTime = OffsetDateTime.now();
        OffsetDateTime endTime = initTime.plusHours(1);

        Verification verification = new Verification();
        verification.setId(UUID.randomUUID());
        verification.setEmail(email);
        verification.setType(type);
        verification.setCode(otp);
        verification.setInitTime(initTime);
        verification.setEndTime(endTime);

        verificationRepository.save(verification);
        mailgunUseCase.sendEmail(email, "Your Commerce OTP Code", "Your OTP code is: " + otp);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
