package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.ResetPasswordRequest;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.providers.ProviderInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.providers.ProviderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void resetPassword(ResetPasswordRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(AccountNotFoundException::new);

        Provider provider = providerRepository.findByAccountId(account.getId())
                .orElseThrow(ProviderNotFoundException::new);

        if (!provider.getName().equals("INTERNAL")) {
            throw new ProviderInvalidException();
        }

        if (verificationUseCase.verifyOtp(request.getEmail(), request.getOtp(), "RESET_PASSWORD")) {
            String encodedPassword = securityConfiguration.encode(request.getNewPassword());
            account.setPassword(encodedPassword);
            accountRepository.saveAndFlush(account);
        } else {
            throw new VerificationInvalidException();
        }
    }
}
