package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.LoginByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByExternalRequest;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.BasicAuthenticationUseCase;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.LoginAuthenticationUseCase;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.RegisterAuthenticationUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountCredentialsInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/authentications")
public class AuthenticationRest {
    @Autowired
    private BasicAuthenticationUseCase basicAuthenticationUseCase;

    @Autowired
    private LoginAuthenticationUseCase loginAuthenticationUseCase;

    @Autowired
    private RegisterAuthenticationUseCase registerAuthenticationUseCase;

    @PostMapping(value = "/logins/email-password")
    public ResponseEntity<ResponseBody<Session>> loginByEmailAndPassword(
            @RequestBody LoginByEmailAndPasswordRequest request
    ) {
        try {
            Session session = loginAuthenticationUseCase.loginByEmailAndPassword(request.getEmail(), request.getPassword());
            return ResponseBody
                    .<Session>builder()
                    .message("Login succeed.")
                    .data(session)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountCredentialsInvalidException e) {
            return ResponseBody
                    .<Session>builder()
                    .message("Account credentials invalid.")
                    .build()
                    .toEntity(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseBody
                    .<Session>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/registers/email-password")
    public ResponseEntity<ResponseBody<Account>> registerByEmailAndPassword(
            @RequestBody RegisterByEmailAndPasswordRequest request
    ) {
        try {
            Account account = registerAuthenticationUseCase.registerByEmailAndPassword(request);
            return ResponseBody
                    .<Account>builder()
                    .message("Register succeed.")
                    .data(account)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountExistsException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Account exists.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/registers/internal")
    public ResponseEntity<ResponseBody<Account>> registerByInternal(
            @RequestBody RegisterByEmailAndPasswordRequest request
    ) {
        try {
            Account account = registerAuthenticationUseCase.registerByInternal(request);
            return ResponseBody
                    .<Account>builder()
                    .message("Register succeed.")
                    .data(account)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountExistsException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Account exists.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (VerificationExpiredException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("OTP is invalid or expired.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/registers/external")
    public ResponseEntity<ResponseBody<Account>> registerByExternal(
            @RequestBody RegisterByExternalRequest request
    ) {
        try {
            Account account = registerAuthenticationUseCase.registerByExternal(request);
            return ResponseBody
                    .<Account>builder()
                    .message("Register succeed.")
                    .data(account)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountExistsException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Account exists.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/logouts/session")
    public ResponseEntity<ResponseBody<Void>> logoutBySession(
            @RequestBody Session session
    ) {
        try {
            basicAuthenticationUseCase.logout(session);
            return ResponseBody
                    .<Void>builder()
                    .message("Logout succeed.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (TokenExpiredException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Access token already expired.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (JWTVerificationException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Session verification failed.")
                    .build()
                    .toEntity(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(value = "/refreshes/session")
    public ResponseEntity<ResponseBody<Session>> refreshSession(
            @RequestBody Session session
    ) {
        try {
            Session newSession = basicAuthenticationUseCase.refreshSession(session);
            return ResponseBody
                    .<Session>builder()
                    .message("Session refreshed.")
                    .data(newSession)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (TokenExpiredException e) {
            return ResponseBody
                    .<Session>builder()
                    .message("Session expired.")
                    .build()
                    .toEntity(HttpStatus.UNAUTHORIZED);
        } catch (JWTVerificationException e) {
            return ResponseBody
                    .<Session>builder()
                    .message("Session verification failed.")
                    .build()
                    .toEntity(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseBody
                    .<Session>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}