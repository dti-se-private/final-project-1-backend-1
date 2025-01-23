package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.usecases.accounts.BasicAccountUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(value = "/accounts")
public class AccountRest {
    @Autowired
    private BasicAccountUseCase basicAccountUseCase;

    @PostMapping
    public ResponseEntity<ResponseBody<Account>> saveOne(
            @RequestBody Account account
    ) {
        try {
            Account savedAccount = basicAccountUseCase.saveOne(account);
            return ResponseBody
                    .<Account>builder()
                    .message("Account saved.")
                    .data(savedAccount)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountExistsException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Account already exists.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ResponseBody<Account>> findOneById(
            @PathVariable("id") UUID id
    ) {
        try {
            Account foundAccount = basicAccountUseCase.findOneById(id);
            return ResponseBody
                    .<Account>builder()
                    .message("Account found.")
                    .data(foundAccount)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<ResponseBody<Account>> patchOneById(
            @PathVariable("id") UUID id,
            @RequestBody Account account
    ) {
        try {
            Account updatedAccount = basicAccountUseCase.patchOneById(id, account);
            return ResponseBody
                    .<Account>builder()
                    .message("Account patched.")
                    .data(updatedAccount)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Account>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ResponseBody<Void>> deleteOneById(
            @PathVariable("id") UUID id
    ) {
        try {
            basicAccountUseCase.deleteOneById(id);
            return ResponseBody
                    .<Void>builder()
                    .message("Account deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
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