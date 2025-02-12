package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.*;
import org.dti.se.finalproject1backend1.inners.usecases.orders.*;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountAddressNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.carts.CartItemInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.PaymentMethodInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductInsufficientException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderRest {

    @Autowired
    CheckoutUseCase checkoutUseCase;
    @Autowired
    OrderUseCase orderUseCase;
    @Autowired
    PaymentUseCase paymentUseCase;
    @Autowired
    CancellationUseCase cancellationUseCase;
    @Autowired
    private ShipmentUseCase shipmentUseCase;


    @PostMapping("/try-checkout")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> tryCheckout(
            @RequestAttribute("transactionStatus") TransactionStatus transactionStatus,
            @AuthenticationPrincipal Account account,
            @RequestBody OrderRequest request
    ) {
        try {
            OrderResponse order = checkoutUseCase.tryCheckout(account, request);
            transactionStatus.setRollbackOnly();
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order check out tried.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (CartItemInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Cart item invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (AccountAddressNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account address not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/checkout")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> checkout(
            @AuthenticationPrincipal Account account,
            @RequestBody OrderRequest request
    ) {
        try {
            OrderResponse order = checkoutUseCase.checkout(account, request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order checked out.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (CartItemInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Cart item invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (AccountAddressNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account address not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/payments/process")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> processPayment(
            @RequestBody PaymentProcessRequest request
    ) {
        try {
            OrderResponse order = paymentUseCase.processPayment(request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order payment processed.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (PaymentMethodInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Payment method invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseProductNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Warehouse product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = orderUseCase
                    .getOrders(account, page, size, search);
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


    @GetMapping("/payment-confirmations")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getPaymentConfirmationOrders(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = paymentUseCase
                    .getPaymentConfirmationOrders(account, page, size, search);
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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<OrderResponse>> processPaymentConfirmation(
            @RequestBody OrderProcessRequest request
    ) {
        try {
            OrderResponse order = paymentUseCase.processPaymentConfirmation(request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order payment confirmation processed.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (OrderStatusInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order status invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (OrderActionInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order action invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseProductNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Warehouse product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/shipment-confirmations/process")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> processShipmentConfirmation(
            @RequestBody OrderProcessRequest request
    ) {
        try {
            OrderResponse order = shipmentUseCase.processShipmentConfirmation(request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order shipment confirmation processed.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (OrderActionInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order action invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancellations/process")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> processCancellation(
            @AuthenticationPrincipal Account account,
            @RequestBody OrderProcessRequest request
    ) {
        try {
            OrderResponse order = cancellationUseCase.processCancellation(account, request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order cancellation processed.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (OrderActionInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order action invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseProductNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Warehouse product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseProductInsufficientException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Warehouse product insufficient.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/payment-gateways/process")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<PaymentGatewayResponse>> processPaymentGateway(
            @RequestBody PaymentGatewayRequest request
    ) {
        try {
            PaymentGatewayResponse paymentGateway = paymentUseCase.processPaymentGateway(request);
            return ResponseBody
                    .<PaymentGatewayResponse>builder()
                    .message("Order payment gateway processed.")
                    .data(paymentGateway)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return ResponseBody
                    .<PaymentGatewayResponse>builder()
                    .message("Order not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (OrderStatusInvalidException e) {
            return ResponseBody
                    .<PaymentGatewayResponse>builder()
                    .message("Order status invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<PaymentGatewayResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
