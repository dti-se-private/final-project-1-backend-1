package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments.ShipmentRateResponse;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.BiteshipGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountAddressNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.carts.CartItemInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.CartCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.customs.LocationCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.*;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutUseCase {
    @Autowired
    OrderUseCase orderUseCase;

    @Autowired
    LocationCustomRepository locationCustomRepository;
    @Autowired
    CartItemRepository cartItemRepository;
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
    AccountAddressRepository accountAddressRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    ProductCustomRepository productCustomRepository;
    @Autowired
    CartCustomRepository cartCustomRepository;

    @Autowired
    @Qualifier("oneTransactionManager")
    PlatformTransactionManager transactionManager;

    @Autowired
    BiteshipGateway biteshipGateway;


    public OrderResponse tryCheckout(Account account, OrderRequest request) {
        return checkout(account, request);
    }

    public OrderResponse checkout(Account account, OrderRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        List<CartItem> foundCartItems = cartItemRepository
                .findAllByAccountId(foundAccount.getId());

        if (foundCartItems.isEmpty()) {
            throw new CartItemInvalidException();
        }

        AccountAddress foundAccountAddress = accountAddressRepository
                .findById(request.getAddressId())
                .orElseThrow(AccountAddressNotFoundException::new);


        Point shipmentDestination = foundAccountAddress.getLocation();
        Warehouse nearestWarehouse = locationCustomRepository.getNearestWarehouse(shipmentDestination);
        Point shipmentOrigin = nearestWarehouse.getLocation();

        ShipmentRateResponse shipmentRateResponse = biteshipGateway.getShipmentRate(
                shipmentOrigin,
                shipmentDestination,
                foundCartItems
        );

        Double itemPrice = cartCustomRepository.getTotalPrice(account.getId());
        Double shipmentPrice = shipmentRateResponse.getPricing().getFirst().getPrice();
        Double totalPrice = itemPrice + shipmentPrice;

        Order newOrder = Order
                .builder()
                .id(UUID.randomUUID())
                .account(foundAccount)
                .totalPrice(totalPrice)
                .itemPrice(itemPrice)
                .shipmentPrice(shipmentPrice)
                .shipmentOrigin(shipmentOrigin)
                .shipmentDestination(shipmentDestination)
                .originWarehouse(nearestWarehouse)
                .build();
        orderRepository.saveAndFlush(newOrder);

        OrderStatus newOrderStatus = OrderStatus
                .builder()
                .id(UUID.randomUUID())
                .order(newOrder)
                .status("WAITING_FOR_PAYMENT")
                .time(now)
                .build();
        orderStatusRepository.saveAndFlush(newOrderStatus);

        List<OrderItem> newOrderItems = new ArrayList<>();
        for (CartItem foundCartItem : foundCartItems) {
            OrderItem newOrderItem = OrderItem
                    .builder()
                    .id(UUID.randomUUID())
                    .order(newOrder)
                    .product(foundCartItem.getProduct())
                    .quantity(foundCartItem.getQuantity())
                    .warehouseLedger(null)
                    .build();
            newOrderItems.add(newOrderItem);
        }
        orderItemRepository.saveAllAndFlush(newOrderItems);

        cartItemRepository.deleteAll(foundCartItems);

        return orderCustomRepository.getOrder(newOrder.getId());
    }
}
