package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.OtpUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountCredentialsInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otps")
public class OtpRest {

    @Autowired
    private OtpUseCase otpUseCase;

    @PostMapping("/send")
    public ResponseEntity<ResponseBody<Void>> sendOtp(@RequestParam String email, @RequestParam String type) {
        try {
            otpUseCase.sendOtp(email, type);
            return ResponseBody
                    .<Void>builder()
                    .message("OTP sent succeed.")
                    .data(null)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
