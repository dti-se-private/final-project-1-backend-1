package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressResponse;
import org.dti.se.finalproject1backend1.inners.usecases.accounts.AccountAddressUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountAddressNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/account-addresses")
public class AccountAddressRest {

    @Autowired
    private AccountAddressUseCase accountAddressUseCase;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<AccountAddressResponse>> addAddress(
            @AuthenticationPrincipal Account account,
            @RequestBody AccountAddressRequest request
    ) {
        try {
            AccountAddressResponse response = accountAddressUseCase.addAddress(account, request);
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Address added.")
                    .data(response)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{addressId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<AccountAddressResponse>> patchAddress(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID addressId,
            @RequestBody AccountAddressRequest request
    ) {
        try {
            AccountAddressResponse response = accountAddressUseCase.patchAddress(account, addressId, request);
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Address patched.")
                    .data(response)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (AccountAddressNotFoundException e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Address not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<AccountAddressResponse>> getAddress(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID addressId
    ) {
        try {
            AccountAddressResponse response = accountAddressUseCase.getAddress(account, addressId);
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Address found.")
                    .data(response)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (AccountAddressNotFoundException e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Address not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<AccountAddressResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<List<AccountAddressResponse>>> getAddresses(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<AccountAddressResponse> foundAddresses = accountAddressUseCase
                    .getAddresses(account, page, size, search);
            return ResponseBody
                    .<List<AccountAddressResponse>>builder()
                    .message("Addresses found.")
                    .data(foundAddresses)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<List<AccountAddressResponse>>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<List<AccountAddressResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<Void>> deleteAddress(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID addressId
    ) {
        try {
            accountAddressUseCase.deleteAddress(account, addressId);
            return ResponseBody
                    .<Void>builder()
                    .message("Address deleted.")
                    .build()
                    .toEntity(HttpStatus.NO_CONTENT);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (AccountAddressNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Address not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
