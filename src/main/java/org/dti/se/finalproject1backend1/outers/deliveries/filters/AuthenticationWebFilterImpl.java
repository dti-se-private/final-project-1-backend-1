package org.dti.se.finalproject1backend1.outers.deliveries.filters;

import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationWebFilterImpl extends OncePerRequestFilter {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AuthenticationManagerImpl authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = getAccessToken(request);
            if (accessToken != null) {
                Session session = sessionRepository.getByAccessToken(accessToken);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        null,
                        session,
                        null
                );
                authenticationManager.authenticate(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (TokenExpiredException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    public String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.split(" ")[1];
        }
        return null;
    }
}