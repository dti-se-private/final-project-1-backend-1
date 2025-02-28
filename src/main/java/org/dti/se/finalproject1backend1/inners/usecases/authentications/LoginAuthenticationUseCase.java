package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.deliveries.filters.AuthenticationManagerImpl;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountCredentialsInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    @Autowired
    AccountPermissionRepository accountPermissionRepository;

    @Autowired
    GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    AuthenticationManagerImpl authenticationManager;

    public Session loginByInternal(String email, String password) {
        Account account = accountRepository
                .findByEmailAndPassword(email, securityConfiguration.encode(password))
                .orElseThrow(AccountCredentialsInvalidException::new);

        return getSession(account);
    }

    public Session loginByExternal(String credential) {
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(credential);
        } catch (GeneralSecurityException | IOException e) {
            throw new VerificationInvalidException();
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(AccountCredentialsInvalidException::new);

        return getSession(account);
    }

    Session getSession(Account account) {
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
                .orElseThrow(AccountPermissionNotFoundException::new); // ganti ke permission not found exception

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
        OffsetDateTime accessTokenExpiredAt = now.plusMinutes(30);
        OffsetDateTime refreshTokenExpiredAt = now.plusDays(3);
        Session session = Session
                .builder()
                .account(accountResponse)
                .accessToken(jwtAuthenticationUseCase.generate(account, accessTokenExpiredAt))
                .refreshToken(jwtAuthenticationUseCase.generate(account, refreshTokenExpiredAt))
                .accessTokenExpiredAt(accessTokenExpiredAt)
                .refreshTokenExpiredAt(refreshTokenExpiredAt)
                .permissions(permissions)
                .providers(providers)
                .build();

        sessionRepository.setByAccessToken(session);
        return session;
    }
}