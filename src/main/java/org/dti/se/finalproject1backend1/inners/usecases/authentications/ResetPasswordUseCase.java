package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.ProviderNotFoundException;
import java.security.ProviderException;

@Service
public class ResetPasswordUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    VerificationUseCase verificationUseCase;

    @Autowired
    SecurityConfiguration securityConfiguration;

    public void resetPassword(String email, String newPassword, String otp) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        Provider provider = providerRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        if (!"INTERNAL".equals(provider.getName())) {
            throw new ProviderException("Reset password is only allowed for register by email and password");
        }

        if (verificationUseCase.verifyOtp(email, otp, "RESET_PASSWORD")) {
            String encodedPassword = securityConfiguration.encode(newPassword);
            account.setPassword(encodedPassword);
            accountRepository.saveAndFlush(account);
        } else {
            throw new VerificationNotFoundException("Invalid OTP");
        }
    }
}
