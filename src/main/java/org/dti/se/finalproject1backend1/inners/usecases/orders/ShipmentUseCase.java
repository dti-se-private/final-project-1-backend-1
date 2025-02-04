package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderStatus;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderActionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ShipmentUseCase {
    @Autowired
    OrderCustomRepository orderCustomRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderStatusRepository orderStatusRepository;

    public OrderResponse processShipmentConfirmation(OrderProcessRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Order foundOrder = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

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
