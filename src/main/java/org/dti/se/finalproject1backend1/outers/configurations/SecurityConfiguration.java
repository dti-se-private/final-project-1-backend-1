package org.dti.se.finalproject1backend1.outers.configurations;

import org.dti.se.finalproject1backend1.outers.deliveries.filters.AuthenticationManagerImpl;
import org.dti.se.finalproject1backend1.outers.deliveries.filters.AuthenticationWebFilterImpl;
import org.dti.se.finalproject1backend1.outers.deliveries.filters.TransactionWebFilterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration implements PasswordEncoder {

    @Autowired
    AuthenticationWebFilterImpl authenticationWebFilterImpl;

    @Autowired
    AuthenticationManagerImpl authenticationManagerImpl;

    @Autowired
    TransactionWebFilterImpl transactionWebFilterImpl;

    @Autowired
    Environment environment;

    public List<String> unAuthenticatedPaths = List.of(
            "/otp/**",
            "/authentications/**",
            "/products/**",
            "/webjars/**",
            "/v3/api-docs/**"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(authenticationManagerImpl)
                .addFilterBefore(transactionWebFilterImpl, DisableEncodeUrlFilter.class)
                .addFilterAt(authenticationWebFilterImpl, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(unAuthenticatedPaths.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return Sha512DigestUtils.shaHex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return Objects.equals(encodedPassword, Sha512DigestUtils.shaHex(rawPassword.toString()));
    }

}