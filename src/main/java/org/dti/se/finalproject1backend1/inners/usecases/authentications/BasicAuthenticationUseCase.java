package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.outers.deliveries.filters.AuthenticationManagerImpl;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    AccountPermissionRepository accountPermissionRepository;

    @Autowired
    AuthenticationManagerImpl authenticationManagerImpl; // Keep the autowired, assume it's adapted

    public void logout(Session session) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                null,
                session.getAccessToken(),
                null
        );
        authenticationManagerImpl.authenticate(authentication);
        sessionRepository.deleteByAccessToken(session.getAccessToken());
    }

    public Session refreshSession(Session session) {
        DecodedJWT jwt = jwtAuthenticationUseCase.verify(session.getRefreshToken());
        UUID accountId = jwt.getClaim("account_id").as(UUID.class);

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        AccountResponse accountResponse = AccountResponse
                .builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .image(account.getImage())
                .isVerified(account.getIsVerified())
                .build();

        List<AccountPermission> permissionsList = accountPermissionRepository
                .findByAccountId(account.getId())
                .orElseThrow(AccountUnAuthorizedException::new);

        List<String> permissions = permissionsList
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        List<String> providers = account
                .getProviders()
                .stream()
                .map(Provider::getName)
                .toList();

        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        String newAccessToken = jwtAuthenticationUseCase.generate(account, now.plusSeconds(30));
        String newRefreshToken = jwtAuthenticationUseCase.generate(account, now.plusDays(3));
        Session newSession = Session
                .builder()
                .account(accountResponse)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiredAt(now.plusSeconds(5))
                .refreshTokenExpiredAt(now.plusDays(3))
                .permissions(permissions)
                .providers(providers)
                .build();

        sessionRepository.setByAccessToken(newSession);
        return newSession;
    }
}