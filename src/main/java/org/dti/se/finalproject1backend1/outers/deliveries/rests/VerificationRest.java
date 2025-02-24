package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.verifications.VerificationRequest;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.VerificationUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verifications")
public class VerificationRest {

    @Autowired
    VerificationUseCase verificationUseCase;

    @PostMapping("/send")
    public ResponseEntity<ResponseBody<Void>> send(
            @RequestBody VerificationRequest request
    ) {
        try {
            verificationUseCase.send(request);
            return ResponseBody
                    .<Void>builder()
                    .message("Verification send succeed.")
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
