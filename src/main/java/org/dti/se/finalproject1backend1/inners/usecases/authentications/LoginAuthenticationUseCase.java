package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Session;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountCredentialsInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class LoginAuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    JwtAuthenticationUseCase jwtAuthenticationUseCase;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    public Session loginByEmailAndPassword(String email, String password) {
        Account account = accountRepository.findFirstByEmailAndPassword(email, securityConfiguration.encode(password));
        if (account == null) {
            throw new AccountCredentialsInvalidException();
        }

        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime accessTokenExpiredAt = now.plusSeconds(30);
        OffsetDateTime refreshTokenExpiredAt = now.plusDays(3);
        Session session = Session
                .builder()
                .accountId(account.getId())
                .accessToken(jwtAuthenticationUseCase.generate(account, accessTokenExpiredAt))
                .refreshToken(jwtAuthenticationUseCase.generate(account, refreshTokenExpiredAt))
                .accessTokenExpiredAt(accessTokenExpiredAt)
                .refreshTokenExpiredAt(refreshTokenExpiredAt)
                .build();

        sessionRepository.setByAccessToken(session);
        return session;
    }
}