package org.dti.se.finalproject1backend1.outers.deliveries.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.JwtAuthenticationUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationManagerImpl implements AuthenticationManager { // Changed to AuthenticationManager
    @Autowired
    private JwtAuthenticationUseCase jwtAuthenticationUseCase;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException { // Removed Mono and throws AuthenticationException
        Session session = (Session) authentication.getCredentials();
        DecodedJWT jwt = jwtAuthenticationUseCase.verify(session.getAccessToken());
        UUID accountId = jwt.getClaim("account_id").as(UUID.class);
        Account account;
        try {
            account = accountRepository.findFirstById(accountId);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }

        return new UsernamePasswordAuthenticationToken(
                account,
                authentication.getCredentials(),
                null
        );
    }
}