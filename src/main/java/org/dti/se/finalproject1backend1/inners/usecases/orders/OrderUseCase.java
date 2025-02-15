package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductInsufficientException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.LocationCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class OrderUseCase {
    @Autowired
    OrderCustomRepository orderCustomRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderStatusRepository orderStatusRepository;
    @Autowired
    WarehouseLedgerRepository warehouseLedgerRepository;
    @Autowired
    WarehouseProductRepository warehouseProductRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private LocationCustomRepository locationCustomRepository;


    public List<OrderResponse> getOrders(
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
                    .getOrders(page, size, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return orderCustomRepository
                    .getOrders(account, page, size, search);
        } else if (accountPermissions.contains("CUSTOMER")) {
            return orderCustomRepository
                    .getCustomerOrders(account, page, size, search);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public void processOrderProcessing(UUID orderId, String ledgerStatus) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        List<OrderItem> foundOrderItems = orderItemRepository
                .findAllByOrderId(foundOrder.getId());

        // Get nearest warehouse product from order shipment origin warehouse.
        for (OrderItem foundOrderItem : foundOrderItems) {
            WarehouseProduct originWarehouseProduct = locationCustomRepository
                    .getNearestExistingWarehouseProduct(
                            foundOrder.getShipmentOrigin(),
                            foundOrderItem.getProduct().getId(),
                            foundOrderItem.getQuantity()
                    );

            if (originWarehouseProduct == null) {
                throw new WarehouseProductNotFoundException();
            }

            if (!originWarehouseProduct.getWarehouse().getId().equals(foundOrder.getOriginWarehouse().getId())) {
                WarehouseProduct destinationWarehouseProduct = warehouseProductRepository
                        .findByProductIdAndWarehouseId(
                                foundOrderItem.getProduct().getId(),
                                foundOrder.getOriginWarehouse().getId()
                        )
                        .orElseThrow(WarehouseProductNotFoundException::new);

                Double originPostQuantity = originWarehouseProduct.getQuantity() - foundOrderItem.getQuantity();
                Double destinationPostQuantity = destinationWarehouseProduct.getQuantity() + foundOrderItem.getQuantity();
                if (originPostQuantity < 0 || destinationPostQuantity < 0) {
                    throw new WarehouseProductInsufficientException();
                }

                WarehouseLedger newWarehouseLedger = WarehouseLedger
                        .builder()
                        .id(UUID.randomUUID())
                        .product(originWarehouseProduct.getProduct())
                        .originWarehouse(originWarehouseProduct.getWarehouse())
                        .destinationWarehouse(destinationWarehouseProduct.getWarehouse())
                        .originPreQuantity(originWarehouseProduct.getQuantity())
                        .originPostQuantity(originPostQuantity)
                        .destinationPreQuantity(destinationWarehouseProduct.getQuantity())
                        .destinationPostQuantity(destinationPostQuantity)
                        .status(ledgerStatus)
                        .build();

                originWarehouseProduct.setQuantity(originPostQuantity);
                destinationWarehouseProduct.setQuantity(destinationPostQuantity);
                warehouseProductRepository.saveAndFlush(originWarehouseProduct);
                warehouseProductRepository.saveAndFlush(destinationWarehouseProduct);
                warehouseLedgerRepository.saveAndFlush(newWarehouseLedger);
            } else {
                Double originPostQuantity = originWarehouseProduct.getQuantity() - foundOrderItem.getQuantity();
                if (originPostQuantity < 0) {
                    throw new WarehouseProductInsufficientException();
                }

                originWarehouseProduct.setQuantity(originPostQuantity);
                warehouseProductRepository.saveAndFlush(originWarehouseProduct);
            }
        }

        OrderStatus newOrderStatus = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("SHIPPING")
                .time(now)
                .build();
        orderStatusRepository.saveAndFlush(newOrderStatus);
    }

}
