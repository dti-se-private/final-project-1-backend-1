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
import org.dti.se.finalproject1backend1.outers.utilities.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    @Qualifier("oneTransactionTemplate")
    TransactionTemplate transactionTemplate;

    @Autowired
    @Qualifier("oneTransactionManager")
    PlatformTransactionManager transactionManager;


    public List<OrderResponse> getOrders(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        if (PermissionUtil.isSuperAdmin(account)) {
            return orderCustomRepository
                    .getOrders(page, size, search);
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            return orderCustomRepository
                    .getOrders(account, page, size, search);
        } else if (PermissionUtil.isCustomer(account)) {
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
        if (PermissionUtil.isSuperAdmin(account)) {
            return orderCustomRepository
                    .getOrder(orderId);
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            return orderCustomRepository
                    .getOrder(account, orderId);
        } else if (PermissionUtil.isCustomer(account)) {
            return orderCustomRepository
                    .getCustomerOrder(account, orderId);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    /**
     * Process order by allocating warehouse inventory.
     * 
     * Performance optimizations:
     * - Uses batch operations to reduce database round-trips
     * - Transaction isolation prevents race conditions
     * - Spatial query optimization via indexed location lookups
     * 
     * @param orderId The order to process
     */
    public void processOrderProcessing(UUID orderId) {
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

        // Transactional stock mutation.
        // Use REQUIRES_NEW to prevent outer transaction rollback.
        // It should use NESTED, but it's not supported by hibernate.
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            // Collect entities for batch operations
            List<StockLedger> stockLedgersToSave = new ArrayList<>();
            List<WarehouseLedger> warehouseLedgersToSave = new ArrayList<>();
            // Use Set to efficiently track unique warehouse products to update
            Set<WarehouseProduct> warehouseProductsToUpdate = new HashSet<>();
            
            for (OrderItem foundOrderItem : foundOrderItems) {
                // Get nearest warehouse product from order shipment origin warehouse.
                WarehouseProduct originWarehouseProduct = locationCustomRepository
                        .getNearestExistingWarehouseProduct(
                                foundOrder.getShipmentOrigin(),
                                foundOrderItem.getProduct().getId()
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

                // Move warehouse stocks first if the nearest origin warehouse is different from destination warehouse.
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
                    stockLedgersToSave.add(originStockLedger);

                    StockLedger destinationStockLedger = StockLedger
                            .builder()
                            .id(UUID.randomUUID())
                            .warehouseProduct(destinationWarehouseProduct)
                            .preQuantity(destinationWarehouseProduct.getQuantity())
                            .postQuantity(destinationPostQuantity)
                            .time(now)
                            .build();
                    stockLedgersToSave.add(destinationStockLedger);

                    WarehouseLedger newWarehouseLedger = WarehouseLedger
                            .builder()
                            .id(UUID.randomUUID())
                            .originWarehouseProduct(originWarehouseProduct)
                            .destinationWarehouseProduct(destinationWarehouseProduct)
                            .originPreQuantity(originWarehouseProduct.getQuantity())
                            .originPostQuantity(originPostQuantity)
                            .destinationPreQuantity(destinationWarehouseProduct.getQuantity())
                            .destinationPostQuantity(destinationPostQuantity)
                            .status("APPROVED")
                            .build();

                    originWarehouseProduct.setQuantity(originPostQuantity);
                    destinationWarehouseProduct.setQuantity(destinationPostQuantity);
                    warehouseProductsToUpdate.add(originWarehouseProduct);
                    warehouseProductsToUpdate.add(destinationWarehouseProduct);
                    warehouseLedgersToSave.add(newWarehouseLedger);
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
                stockLedgersToSave.add(destinationStockLedger);

                destinationWarehouseProduct.setQuantity(warehouseProductQuantity);
                // Set automatically handles duplicates efficiently
                warehouseProductsToUpdate.add(destinationWarehouseProduct);
            }
            
            // Batch save all entities to reduce database round-trips
            // Flush all at once for better transaction efficiency
            if (!stockLedgersToSave.isEmpty()) {
                stockLedgerRepository.saveAll(stockLedgersToSave);
            }
            if (!warehouseProductsToUpdate.isEmpty()) {
                warehouseProductRepository.saveAll(warehouseProductsToUpdate);
            }
            if (!warehouseLedgersToSave.isEmpty()) {
                warehouseLedgerRepository.saveAll(warehouseLedgersToSave);
            }
            // Single flush after all saves
            if (!stockLedgersToSave.isEmpty() || !warehouseProductsToUpdate.isEmpty() || !warehouseLedgersToSave.isEmpty()) {
                stockLedgerRepository.flush();
            }
            
            transactionManager.commit(status);
        } catch (Exception exception) {
            transactionManager.rollback(status);
            OrderStatus newOrderStatusCanceled = OrderStatus
                    .builder()
                    .id(UUID.randomUUID())
                    .order(foundOrder)
                    .status("CANCELED")
                    .time(now.plusSeconds(1))
                    .build();
            orderStatusRepository.saveAndFlush(newOrderStatusCanceled);
            throw exception;
        }
    }
}
