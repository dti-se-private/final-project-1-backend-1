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
    OrderItemRepository orderItemRepository;
    @Autowired
    LocationCustomRepository locationCustomRepository;
    @Autowired
    StockLedgerRepository stockLedgerRepository;


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


    public OrderResponse getOrder(
            Account account,
            UUID orderId
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return orderCustomRepository
                    .getOrder(orderId);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return orderCustomRepository
                    .getOrder(account, orderId);
        } else if (accountPermissions.contains("CUSTOMER")) {
            return orderCustomRepository
                    .getCustomerOrder(account, orderId);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public void processOrderProcessing(UUID orderId, String ledgerStatus) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        OrderStatus newOrderStatusProcessing = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("PROCESSING")
                .time(now)
                .build();
        orderStatusRepository.saveAndFlush(newOrderStatusProcessing);

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

            WarehouseProduct destinationWarehouseProduct = warehouseProductRepository
                    .findByProductIdAndWarehouseId(
                            foundOrderItem.getProduct().getId(),
                            foundOrder.getOriginWarehouse().getId()
                    )
                    .orElseThrow(WarehouseProductNotFoundException::new);

            // If order origin warehouse is different from nearest warehouse.
            // Assumes both sides have a warehouse product.
            if (!originWarehouseProduct.getWarehouse().getId().equals(destinationWarehouseProduct.getWarehouse().getId())) {
                Double originPostQuantity = originWarehouseProduct.getQuantity() - foundOrderItem.getQuantity();
                Double destinationPostQuantity = destinationWarehouseProduct.getQuantity() + foundOrderItem.getQuantity();
                if (originPostQuantity < 0 || destinationPostQuantity < 0) {
                    throw new WarehouseProductInsufficientException();
                }

                StockLedger originStockLedger = StockLedger
                        .builder()
                        .id(UUID.randomUUID())
                        .warehouseProduct(originWarehouseProduct)
                        .preQuantity(originWarehouseProduct.getQuantity())
                        .postQuantity(originPostQuantity)
                        .time(now)
                        .build();
                stockLedgerRepository.saveAndFlush(originStockLedger);

                StockLedger destinationStockLedger = StockLedger
                        .builder()
                        .id(UUID.randomUUID())
                        .warehouseProduct(destinationWarehouseProduct)
                        .preQuantity(destinationWarehouseProduct.getQuantity())
                        .postQuantity(destinationPostQuantity)
                        .time(now)
                        .build();
                stockLedgerRepository.saveAndFlush(destinationStockLedger);

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
            }

            // Use warehouse product for order item.
            Double warehouseProductQuantity = destinationWarehouseProduct.getQuantity() - foundOrderItem.getQuantity();
            if (warehouseProductQuantity < 0) {
                throw new WarehouseProductInsufficientException();
            }

            StockLedger destinationStockLedger = StockLedger
                    .builder()
                    .id(UUID.randomUUID())
                    .warehouseProduct(destinationWarehouseProduct)
                    .preQuantity(destinationWarehouseProduct.getQuantity())
                    .postQuantity(warehouseProductQuantity)
                    .time(now)
                    .build();
            stockLedgerRepository.saveAndFlush(destinationStockLedger);

            destinationWarehouseProduct.setQuantity(warehouseProductQuantity);
            warehouseProductRepository.saveAndFlush(destinationWarehouseProduct);
        }

        OrderStatus newOrderStatusShipping = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("SHIPPING")
                .time(now)
                .build();
        orderStatusRepository.saveAndFlush(newOrderStatusShipping);
    }

}
