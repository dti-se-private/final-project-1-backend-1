package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductInsufficientException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseLedgerRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CancellationUseCase {
    @Autowired
    OrderStatusRepository orderStatusRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    WarehouseLedgerRepository warehouseLedgerRepository;
    @Autowired
    WarehouseProductRepository warehouseProductRepository;
    @Autowired
    OrderCustomRepository orderCustomRepository;

    public OrderResponse processCancellation(
            Account account,
            OrderProcessRequest request
    ) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        if (!request.getAction().equals("CANCEL")) {
            throw new OrderActionInvalidException();
        }

        if (accountPermissions.contains("CUSTOMER")) {
            List<String> validCancelStatuses = List.of("WAITING_FOR_PAYMENT", "WAITING_FOR_PAYMENT_CONFIRMATION");
            Boolean isValidStatus = foundOrder
                    .getOrderStatuses()
                    .stream()
                    .max(Comparator.comparing(OrderStatus::getTime))
                    .stream()
                    .anyMatch(orderStatus -> validCancelStatuses.contains(orderStatus.getStatus()));
            if (!isValidStatus) {
                throw new OrderStatusInvalidException();
            }
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN") || accountPermissions.contains("SUPER_ADMIN")) {
            List<String> validCancelStatuses = List.of("WAITING_FOR_PAYMENT", "WAITING_FOR_PAYMENT_CONFIRMATION", "PROCESSING");
            Boolean isValidStatus = foundOrder
                    .getOrderStatuses()
                    .stream()
                    .max(Comparator.comparing(OrderStatus::getTime))
                    .stream()
                    .anyMatch(orderStatus -> validCancelStatuses.contains(orderStatus.getStatus()));
            if (!isValidStatus) {
                throw new OrderStatusInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        // reverse stock from order ledger origin and destination with the current quantity.
        List<WarehouseLedger> foundWarehouseLedgers = foundOrder
                .getOrderItems()
                .stream()
                .map(OrderItem::getWarehouseLedger)
                .toList();

        for (WarehouseLedger foundWarehouseLedger : foundWarehouseLedgers) {
            if (foundWarehouseLedger == null) {
                continue;
            }

            WarehouseProduct originWarehouseProduct = foundWarehouseLedger.getOriginWarehouseProduct();
            WarehouseProduct destinationWarehouseProduct = foundWarehouseLedger.getDestinationWarehouseProduct();

            OrderItem foundOrderItem = foundWarehouseLedger.getOrderItem();

            Double originPostQuantity = destinationWarehouseProduct.getQuantity() - foundOrderItem.getQuantity();
            Double destinationPostQuantity = originWarehouseProduct.getQuantity() + foundOrderItem.getQuantity();

            if (originPostQuantity < 0 || destinationPostQuantity < 0) {
                throw new WarehouseProductInsufficientException();
            }

            WarehouseLedger newWarehouseLedger = WarehouseLedger
                    .builder()
                    .id(UUID.randomUUID())
                    .originWarehouseProduct(foundWarehouseLedger.getDestinationWarehouseProduct())
                    .destinationWarehouseProduct(foundWarehouseLedger.getOriginWarehouseProduct())
                    .originPreQuantity(destinationWarehouseProduct.getQuantity())
                    .originPostQuantity(originPostQuantity)
                    .destinationPreQuantity(originWarehouseProduct.getQuantity())
                    .destinationPostQuantity(destinationPostQuantity)
                    .time(now)
                    .status("APPROVED")
                    .build();

            destinationWarehouseProduct.setQuantity(newWarehouseLedger.getOriginPostQuantity());
            originWarehouseProduct.setQuantity(newWarehouseLedger.getDestinationPostQuantity());

            warehouseLedgerRepository.saveAndFlush(newWarehouseLedger);
            warehouseProductRepository.saveAndFlush(destinationWarehouseProduct);
            warehouseProductRepository.saveAndFlush(originWarehouseProduct);
        }

        OrderStatus newOrderStatus = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("CANCELED")
                .time(now)
                .build();
        orderStatusRepository.saveAndFlush(newOrderStatus);

        return orderCustomRepository.getOrder(foundOrder.getId());
    }
}
