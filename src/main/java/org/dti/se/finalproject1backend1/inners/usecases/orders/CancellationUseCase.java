package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductInsufficientException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseLedgerRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
            Boolean isExistsProcessingStatus = foundOrder
                    .getOrderStatuses()
                    .stream()
                    .anyMatch(orderStatus -> orderStatus.getStatus().equals("PROCESSING"));
            if (isExistsProcessingStatus) {
                throw new OrderStatusInvalidException();
            }
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN") || accountPermissions.contains("SUPER_ADMIN")) {
            Boolean isExistsShippingStatus = foundOrder
                    .getOrderStatuses()
                    .stream()
                    .anyMatch(orderStatus -> orderStatus.getStatus().equals("SHIPPING"));
            if (isExistsShippingStatus) {
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

            Optional<WarehouseProduct> foundOriginWarehouseProduct = warehouseProductRepository
                    .findByProductIdAndWarehouseId(
                            foundWarehouseLedger.getProduct().getId(),
                            foundWarehouseLedger.getOriginWarehouse().getId()
                    );
            if (foundOriginWarehouseProduct.isEmpty()) {
                throw new WarehouseProductNotFoundException();
            }

            Optional<WarehouseProduct> foundDestinationWarehouseProduct = warehouseProductRepository
                    .findByProductIdAndWarehouseId(
                            foundWarehouseLedger.getProduct().getId(),
                            foundWarehouseLedger.getDestinationWarehouse().getId()
                    );
            if (foundDestinationWarehouseProduct.isEmpty()) {
                throw new WarehouseProductNotFoundException();
            }

            OrderItem foundOrderItem = foundWarehouseLedger
                    .getOrderItem();

            Double originPostQuantity = foundDestinationWarehouseProduct.get().getQuantity() - foundOrderItem.getQuantity();
            Double destinationPostQuantity = foundOriginWarehouseProduct.get().getQuantity() + foundOrderItem.getQuantity();

            if (originPostQuantity < 0 || destinationPostQuantity < 0) {
                throw new WarehouseProductInsufficientException();
            }

            WarehouseLedger newWarehouseLedger = WarehouseLedger
                    .builder()
                    .id(UUID.randomUUID())
                    .product(foundWarehouseLedger.getProduct())
                    .originWarehouse(foundWarehouseLedger.getDestinationWarehouse())
                    .destinationWarehouse(foundWarehouseLedger.getOriginWarehouse())
                    .originPreQuantity(foundDestinationWarehouseProduct.get().getQuantity())
                    .originPostQuantity(originPostQuantity)
                    .destinationPreQuantity(foundOriginWarehouseProduct.get().getQuantity())
                    .destinationPostQuantity(destinationPostQuantity)
                    .time(now)
                    .status("APPROVED")
                    .build();

            foundDestinationWarehouseProduct.get().setQuantity(newWarehouseLedger.getOriginPostQuantity());
            foundOriginWarehouseProduct.get().setQuantity(newWarehouseLedger.getDestinationPostQuantity());

            warehouseLedgerRepository.saveAndFlush(newWarehouseLedger);
            warehouseProductRepository.saveAndFlush(foundDestinationWarehouseProduct.get());
            warehouseProductRepository.saveAndFlush(foundOriginWarehouseProduct.get());
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
