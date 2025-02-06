package org.dti.se.finalproject1backend1.outers.deliveries.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.JwtAuthenticationUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        List<GrantedAuthority> authorities = session.getPermissions().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(
                account,
                authentication.getCredentials(),
                authorities
        );
    }
}