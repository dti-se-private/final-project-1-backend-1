package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
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

    public List<OrderResponse> getCustomerOrders(
            Account account,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        return orderCustomRepository
                .getCustomerOrders(foundAccount, page, size, filters, search);
    }

    public List<OrderResponse> getOrders(
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
                    .getOrders(page, size, filters, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return orderCustomRepository
                    .getOrders(account, page, size, filters, search);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public void processOrder(UUID orderId, String ledgerStatus) {
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

                WarehouseLedger newWarehouseLedger = WarehouseLedger
                        .builder()
                        .id(UUID.randomUUID())
                        .product(originWarehouseProduct.getProduct())
                        .originWarehouse(originWarehouseProduct.getWarehouse())
                        .destinationWarehouse(foundOrder.getOriginWarehouse())
                        .originPreQuantity(originWarehouseProduct.getQuantity())
                        .originPostQuantity(originWarehouseProduct.getQuantity() - foundOrderItem.getQuantity())
                        .destinationPreQuantity(destinationWarehouseProduct.getQuantity())
                        .destinationPostQuantity(destinationWarehouseProduct.getQuantity() + foundOrderItem.getQuantity())
                        .status(ledgerStatus)
                        .build();

                originWarehouseProduct.setQuantity(newWarehouseLedger.getOriginPostQuantity());
                destinationWarehouseProduct.setQuantity(newWarehouseLedger.getDestinationPostQuantity());
                warehouseProductRepository.save(originWarehouseProduct);
                warehouseProductRepository.save(destinationWarehouseProduct);
                warehouseLedgerRepository.save(newWarehouseLedger);
            } else {
                originWarehouseProduct.setQuantity(originWarehouseProduct.getQuantity() - foundOrderItem.getQuantity());
                warehouseProductRepository.save(originWarehouseProduct);
            }
        }

        OrderStatus newOrderStatus = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(foundOrder)
                .status("SHIPPING")
                .time(now)
                .build();
        orderStatusRepository.save(newOrderStatus);
    }

}
