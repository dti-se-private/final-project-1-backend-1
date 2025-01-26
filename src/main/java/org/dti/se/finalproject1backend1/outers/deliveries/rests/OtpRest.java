package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.usecases.authentications.OtpUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpRest {

    @Autowired
    private OtpUseCase otpUseCase;

    @PostMapping("/send")
    public void sendOtp(@RequestParam String email, @RequestParam String type) {
        otpUseCase.sendOtp(email, type);
    }
}
