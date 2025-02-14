package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.payments.PaymentLinkResponse;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.MidtransGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.PaymentMethodInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.PaymentProofRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentUseCase {
    @Autowired
    OrderUseCase orderUseCase;
    @Autowired
    OrderCustomRepository orderCustomRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderStatusRepository orderStatusRepository;
    @Autowired
    PaymentProofRepository paymentProofRepository;
    @Autowired
    MidtransGateway midtransGateway;

    public PaymentGatewayResponse processPaymentGateway(PaymentGatewayRequest request) {
        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        List<OrderStatus> orderStatuses = orderStatusRepository
                .findAllByOrderIdOrderByTimeAsc(foundOrder.getId());

        Boolean isLastWaitingForPaymentStatus = orderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT");
        if (!isLastWaitingForPaymentStatus) {
            throw new OrderStatusInvalidException();
        }

        List<OrderItem> orderItems = foundOrder
                .getOrderItems()
                .stream()
                .toList();

        PaymentLinkResponse paymentLinkResponse = midtransGateway
                .getPaymentLinkUrl(
                        foundOrder.getId(),
                        foundOrder.getTotalPrice(),
                        orderItems
                );

        return PaymentGatewayResponse
                .builder()
                .url(paymentLinkResponse.getPaymentUrl())
                .build();
    }

    public OrderResponse processPayment(PaymentProcessRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        if (request.getPaymentMethod().equals("AUTOMATIC")) {
            orderUseCase.processOrderProcessing(request.getOrderId(), "APPROVED");
        } else if (request.getPaymentMethod().equals("MANUAL")) {
            OrderStatus newOrderStatus = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("WAITING_FOR_PAYMENT_CONFIRMATION")
                    .time(now)
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatus);

            List<PaymentProof> newPaymentProofs = request
                    .getPaymentProofs()
                    .stream()
                    .map(paymentProofRequest -> PaymentProof
                            .builder()
                            .id(UUID.randomUUID())
                            .order(foundOrder)
                            .file(paymentProofRequest.getFile())
                            .extension(paymentProofRequest.getExtension())
                            .time(now)
                            .build()
                    )
                    .toList();
            paymentProofRepository.saveAllAndFlush(newPaymentProofs);
        } else {
            throw new PaymentMethodInvalidException();
        }

        return orderCustomRepository.getOrder(request.getOrderId());
    }

    public List<OrderResponse> getPaymentConfirmationOrders(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return orderCustomRepository
                    .getPaymentConfirmationOrders(page, size, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return orderCustomRepository
                    .getPaymentConfirmationOrders(account, page, size, search);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public OrderResponse processPaymentConfirmation(OrderProcessRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        List<OrderStatus> orderStatuses = orderStatusRepository
                .findAllByOrderIdOrderByTimeAsc(foundOrder.getId());

        Boolean isLastWaitingForPaymentConfirmationStatus = orderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION");
        if (!isLastWaitingForPaymentConfirmationStatus) {
            throw new OrderStatusInvalidException();
        }

        if (request.getAction().equals("APPROVE")) {
            OrderStatus newOrderStatus = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("PROCESSING")
                    .time(now)
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatus);
            orderUseCase.processOrderProcessing(request.getOrderId(), "APPROVED");
        } else if (request.getAction().equals("REJECT")) {
            OrderStatus newOrderStatus = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("WAITING_FOR_PAYMENT")
                    .time(now)
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatus);
        } else {
            throw new OrderActionInvalidException();
        }

        return orderCustomRepository.getOrder(request.getOrderId());
    }
}
