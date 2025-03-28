package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.verifications.VerificationRequest;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.GmailGateway;
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
public class VerificationUseCase {

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    GmailGateway gmailGateway;

    public void send(VerificationRequest request) {
        String otp = generateOtp();
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime endTime = now.plusHours(1);

        Verification verification = new Verification();
        verification.setId(UUID.randomUUID());
        verification.setEmail(request.getEmail());
        verification.setType(request.getType());
        verification.setCode(otp);
        verification.setInitTime(now);
        verification.setEndTime(endTime);

        verificationRepository.saveAndFlush(verification);

        gmailGateway.sendEmail(
                verification.getEmail(),
                "Your Ecommerce OTP Code",
                "Your " + verification.getType().toLowerCase().replace("_", " ") + " OTP code is: " + otp
        );
    }

    public boolean verifyOtp(String email, String otp, String type) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Verification verification = verificationRepository
                .findByEmailAndCodeAndType(email, otp, type)
                .orElseThrow(VerificationNotFoundException::new);

        verificationRepository.delete(verification);

        if (now.isAfter(verification.getEndTime())) {
            throw new VerificationExpiredException();
        }

        return true;
    }

    String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
