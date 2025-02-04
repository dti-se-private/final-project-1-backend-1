package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderStatus;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.PaymentProcessRequest;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.PaymentMethodInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
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
        } else {
            throw new PaymentMethodInvalidException();
        }

        return orderCustomRepository.getOrder(request.getOrderId());
    }

    public List<OrderResponse> getPaymentConfirmationOrders(
            Account account,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return orderCustomRepository
                    .getPaymentConfirmationOrders(page, size, filters, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return orderCustomRepository
                    .getPaymentConfirmationOrders(account, page, size, filters, search);
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

        Boolean isLastForPaymentConfirmationStatus = orderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION");
        if (!isLastForPaymentConfirmationStatus) {
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
