package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.MailgunGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationExpiredException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Service
public class OtpUseCase {

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private MailgunGateway mailgunGateway;

    public void sendOtp(String email, String type) {
        String otp = generateOtp();
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime endTime = now.plusHours(1);

        Verification verification = new Verification();
        verification.setId(UUID.randomUUID());
        verification.setEmail(email);
        verification.setType(type);
        verification.setCode(otp);
        verification.setInitTime(now);
        verification.setEndTime(endTime);

        verificationRepository.saveAndFlush(verification);

        mailgunGateway.sendEmail(email, "Your Commerce OTP Code", "Your " + type.toLowerCase().replace("_", " ") + " OTP code is: " + otp);
    }

    public boolean verifyOtp(String email, String otp, String type) {
        Verification verification = verificationRepository
                .findByEmailAndCodeAndType(email, otp, type)
                .orElseThrow(() -> new VerificationNotFoundException("Invalid OTP"));

        OffsetDateTime now = OffsetDateTime.now();
        if (now.isAfter(verification.getEndTime())) {
            throw new VerificationExpiredException("OTP has expired");
        }

        verificationRepository.delete(verification);

        return true;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
