package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderStatus;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentUseCase {
    @Autowired
    OrderCustomRepository orderCustomRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderStatusRepository orderStatusRepository;


    public List<OrderResponse> getShipmentStartConfirmationOrders(
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
                    .getShipmentStartConfirmationOrders(page, size, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return orderCustomRepository
                    .getShipmentStartConfirmationOrders(account, page, size, search);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public OrderResponse processShipmentStartConfirmation(OrderProcessRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        List<String> validStatuses = List.of("PROCESSING");
        Boolean isValidStatus = foundOrder
                .getOrderStatuses()
                .stream()
                .max(Comparator.comparing(OrderStatus::getTime))
                .stream()
                .anyMatch(orderStatus -> validStatuses.contains(orderStatus.getStatus()));
        if (!isValidStatus) {
            throw new OrderStatusInvalidException();
        }

        if (request.getAction().equals("APPROVE")) {
            OrderStatus newOrderStatusShipping = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("SHIPPING")
                    .time(now)
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatusShipping);
        } else if (request.getAction().equals("REJECT")) {
            OrderStatus newOrderStatusCanceled = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("CANCELED")
                    .time(now)
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatusCanceled);
        } else {
            throw new OrderActionInvalidException();
        }

        return orderCustomRepository.getOrder(request.getOrderId());
    }

    public OrderResponse processShipmentConfirmation(Account account, OrderProcessRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("CUSTOMER")) {
            if (!foundOrder.getAccount().getId().equals(account.getId())) {
                throw new AccountPermissionInvalidException();
            }
        }

        List<String> validStatuses = List.of("SHIPPING");
        Boolean isValidStatus = foundOrder
                .getOrderStatuses()
                .stream()
                .max(Comparator.comparing(OrderStatus::getTime))
                .stream()
                .anyMatch(orderStatus -> validStatuses.contains(orderStatus.getStatus()));
        if (!isValidStatus) {
            throw new OrderStatusInvalidException();
        }

        if (request.getAction().equals("APPROVE")) {
            OrderStatus newOrderStatus = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("ORDER_CONFIRMED")
                    .time(now)
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatus);
        } else {
            throw new OrderActionInvalidException();
        }

        return orderCustomRepository.getOrder(request.getOrderId());
    }
}
