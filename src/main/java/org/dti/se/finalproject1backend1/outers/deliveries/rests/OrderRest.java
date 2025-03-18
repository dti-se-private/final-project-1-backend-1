package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.*;
import org.dti.se.finalproject1backend1.inners.usecases.orders.*;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountAddressNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.blobs.ObjectSizeExceededException;
import org.dti.se.finalproject1backend1.outers.exceptions.carts.CartItemInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.*;
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
import java.util.UUID;

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
    ShipmentUseCase shipmentUseCase;


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
        } catch (ShipmentInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Shipment invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
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
        } catch (ShipmentInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Shipment invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
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

    @PostMapping("/automatic-payments/process")
    public ResponseEntity<ResponseBody<OrderResponse>> processAutomaticPayment(
            @RequestBody AutomaticPaymentProcessRequest request
    ) {
        try {
            OrderResponse order = paymentUseCase.processAutomaticPayment(request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order automatic payment processed.")
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

    @PostMapping("/manual-payments/process")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> processManualPayment(
            @AuthenticationPrincipal Account account,
            @RequestBody ManualPaymentProcessRequest request
    ) {
        try {
            OrderResponse order = paymentUseCase.processManualPayment(account, request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order manual payment processed.")
                    .data(order)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (ObjectSizeExceededException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Object size exceeded.")
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

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> getOrders(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID orderId
    ) {
        try {
            OrderResponse orders = orderUseCase
                    .getOrder(account, orderId);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Orders found.")
                    .data(orders)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account not found.")
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

    @GetMapping("/shipment-start-confirmations")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<OrderResponse>>> getShipmentStartConfirmationOrders(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<OrderResponse> orders = shipmentUseCase
                    .getShipmentStartConfirmationOrders(account, page, size, search);
            return ResponseBody
                    .<List<OrderResponse>>builder()
                    .message("Shipment start confirmation orders found.")
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


    @PostMapping("/shipment-start-confirmations/process")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<OrderResponse>> processShipmentStartConfirmation(
            @RequestBody OrderProcessRequest request
    ) {
        try {
            OrderResponse order = shipmentUseCase.processShipmentStartConfirmation(request);
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Order shipment start confirmation processed.")
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
            @AuthenticationPrincipal Account account,
            @RequestBody OrderProcessRequest request
    ) {
        try {
            OrderResponse order = shipmentUseCase.processShipmentConfirmation(account, request);
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
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<OrderResponse>builder()
                    .message("Account permission invalid.")
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
        } catch (PaymentLinkInvalidException e) {
            return ResponseBody
                    .<PaymentGatewayResponse>builder()
                    .message("Payment link invalid.")
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
