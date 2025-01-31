package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.inners.usecases.orders.OrderUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderRest {

    @Autowired
    OrderUseCase orderUseCase;

    @GetMapping("/customer")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getCustomerOrders(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") List<String> filters,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = orderUseCase
                    .getCustomerOrders(account, page, size, filters, search);
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Orders found.")
                    .data(orders)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") List<String> filters,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = orderUseCase
                    .getOrders(page, size, filters, search);
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Orders found.")
                    .data(orders)
                    .build()
                    .toEntity(HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
