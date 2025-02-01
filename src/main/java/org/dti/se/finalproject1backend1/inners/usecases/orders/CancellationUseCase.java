package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
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

    public void processCancellation(
            OrderProcessRequest request
    ) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        if (!request.getAction().equals("CANCEL")) {
            throw new OrderActionInvalidException();
        }

        Boolean isExistsShippingStatus = foundOrder
                .getOrderStatuses()
                .stream()
                .anyMatch(orderStatus -> orderStatus.getStatus().equals("SHIPPING"));

        if (isExistsShippingStatus) {
            throw new OrderStatusInvalidException();
        }

        // reverse stock from order ledger origin and destination with the current quantity.
        List<WarehouseLedger> foundWarehouseLedgers = foundOrder
                .getOrderItems()
                .stream()
                .map(OrderItem::getWarehouseLedger)
                .toList();

        for (WarehouseLedger foundWarehouseLedger : foundWarehouseLedgers) {
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

            WarehouseLedger newWarehouseLedger = WarehouseLedger
                    .builder()
                    .id(UUID.randomUUID())
                    .product(foundWarehouseLedger.getProduct())
                    .originWarehouse(foundWarehouseLedger.getDestinationWarehouse())
                    .destinationWarehouse(foundWarehouseLedger.getOriginWarehouse())
                    .originPreQuantity(foundDestinationWarehouseProduct.get().getQuantity())
                    .originPostQuantity(foundDestinationWarehouseProduct.get().getQuantity())
                    .destinationPreQuantity(foundOriginWarehouseProduct.get().getQuantity())
                    .destinationPostQuantity(foundOriginWarehouseProduct.get().getQuantity() + foundOrderItem.getQuantity())
                    .time(now)
                    .build();

            warehouseLedgerRepository.save(newWarehouseLedger);
        }

        OrderStatus newOrderStatus = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("CANCELED")
                .time(now)
                .build();
        orderStatusRepository.save(newOrderStatus);
    }
}
