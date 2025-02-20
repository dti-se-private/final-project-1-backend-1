package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.payments.CreatePaymentLinkResponse;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.MidtransGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.blobs.ObjectSizeExceededException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.PaymentProofRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

        CreatePaymentLinkResponse createPaymentLinkResponse = midtransGateway
                .getPaymentLinkUrl(foundOrder, foundOrder.getTotalPrice());

        return PaymentGatewayResponse
                .builder()
                .url(createPaymentLinkResponse.getPaymentUrl())
                .build();
    }

    public OrderResponse processManualPayment(ManualPaymentProcessRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        List<OrderStatus> orderStatuses = orderStatusRepository
                .findAllByOrderIdOrderByTimeAsc(foundOrder.getId());

        Boolean isLastWaitingForPaymentStatus = orderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT");
        if (!isLastWaitingForPaymentStatus) {
            throw new OrderStatusInvalidException();
        }

        OrderStatus newOrderStatus = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("WAITING_FOR_PAYMENT_CONFIRMATION")
                .time(now)
                .build();
        orderStatusRepository.saveAndFlush(newOrderStatus);

        List<PaymentProof> newPaymentProofs = new ArrayList<>();
        for (PaymentProofRequest paymentProofRequest : request.getPaymentProofs()) {
            if (paymentProofRequest.getFile() != null && paymentProofRequest.getFile().length > 1024000) {
                throw new ObjectSizeExceededException();
            }
            PaymentProof newPaymentProof = PaymentProof
                        .builder()
                        .id(UUID.randomUUID())
                        .order(foundOrder)
                        .file(paymentProofRequest.getFile())
                        .extension(paymentProofRequest.getExtension())
                        .time(now)
                        .build();
            newPaymentProofs.add(newPaymentProof);
        }
        paymentProofRepository.saveAllAndFlush(newPaymentProofs);

        return orderCustomRepository.getOrder(request.getOrderId());
    }


    public OrderResponse processAutomaticPayment(AutomaticPaymentProcessRequest request) {
        String parsedUUID = request.getOrderId().substring(0, 36);
        UUID orderId = UUID.fromString(parsedUUID);

        Order foundOrder = orderRepository
                .findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        orderUseCase.processOrderProcessing(foundOrder.getId(), "APPROVED");

        return orderCustomRepository.getOrder(orderId);
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
