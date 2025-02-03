package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.inners.usecases.orders.CancellationUseCase;
import org.dti.se.finalproject1backend1.inners.usecases.orders.OrderUseCase;
import org.dti.se.finalproject1backend1.inners.usecases.orders.PaymentConfirmationUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderRest {

    @Autowired
    OrderUseCase orderUseCase;
    @Autowired
    PaymentConfirmationUseCase paymentConfirmationUseCase;
    @Autowired
    CancellationUseCase cancellationUseCase;

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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") List<String> filters,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = orderUseCase
                    .getOrders(account, page, size, filters, search);
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Orders found.")
                    .data(orders)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment-confirmations")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getPaymentConfirmationOrders(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") List<String> filters,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = paymentConfirmationUseCase
                    .getPaymentConfirmationOrders(account, page, size, filters, search);
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Payment confirmation orders found.")
                    .data(orders)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/payment-confirmations/process")
    public ResponseEntity<ResponseBody<Void>> processPaymentConfirmation(
            @RequestBody OrderProcessRequest request
    ) {
        try {
            paymentConfirmationUseCase.processPaymentConfirmation(request);
            return ResponseBody
                    .<Void>builder()
                    .message("Order payment confirmation processed.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (OrderStatusInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Order status invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (OrderActionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Order action invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancellations/process")
    public ResponseEntity<ResponseBody<Void>> processCancellation(
            @RequestBody OrderProcessRequest request
    ) {
        try {
            cancellationUseCase.processCancellation(request);
            return ResponseBody
                    .<Void>builder()
                    .message("Order cancellation processed.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (OrderActionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Order action invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseProductNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse product not found.")
                    .exception(e)
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
