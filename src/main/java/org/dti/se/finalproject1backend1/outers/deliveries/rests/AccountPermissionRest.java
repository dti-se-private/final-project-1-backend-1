package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountPermissionResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminResponse;
import org.dti.se.finalproject1backend1.inners.usecases.accounts.AccountPermissionUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account-permissions")
public class AccountPermissionRest {

    @Autowired
    private AccountPermissionUseCase accountPermissionUseCase;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<AccountPermissionResponse>> getAccountPermission(
            @AuthenticationPrincipal Account account
    ){
        try {
            AccountPermissionResponse accountPermission = accountPermissionUseCase.getAccountPermissions(account);
            return ResponseBody
                    .<AccountPermissionResponse>builder()
                    .message("Account permission retrieved.")
                    .data(accountPermission)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<AccountPermissionResponse>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (AccountPermissionNotFoundException e) {
            return ResponseBody
                    .<AccountPermissionResponse>builder()
                    .message("Account permission not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseBody
                    .<AccountPermissionResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build());
        }
    }
}
