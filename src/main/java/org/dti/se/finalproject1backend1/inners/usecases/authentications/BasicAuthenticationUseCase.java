package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.outers.deliveries.filters.AuthenticationManagerImpl;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BasicAuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    JwtAuthenticationUseCase jwtAuthenticationUseCase;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    AuthenticationManagerImpl authenticationManagerImpl; // Keep the autowired, assume it's adapted

    public void logout(Session session) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(null, session, null);
        authenticationManagerImpl.authenticate(authentication);
        sessionRepository.deleteByAccessToken(session.getAccessToken());
    }

    public Session refreshSession(Session session) {
        DecodedJWT jwt = jwtAuthenticationUseCase.verify(session.getRefreshToken());
        UUID accountId = jwt.getClaim("account_id").as(UUID.class);

        Account account = accountRepository.findFirstById(accountId);
        if (account == null) {
            throw new AccountNotFoundException();
        }

        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        String newAccessToken = jwtAuthenticationUseCase.generate(account, now.plusSeconds(30));
        String newRefreshToken = jwtAuthenticationUseCase.generate(account, now.plusDays(3));
        Session newSession = Session
                .builder()
                .accountId(account.getId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiredAt(now.plusSeconds(5))
                .refreshTokenExpiredAt(now.plusDays(3))
                .build();
        sessionRepository.setByAccessToken(newSession);
        return newSession;
    }
}