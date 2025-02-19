package org.dti.se.finalproject1backend1.outers.scheduler;

import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderStatusCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class OrderStatusScheduler {

    @Autowired
    OrderStatusCustomRepository orderStatusCustomRepository;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void scheduleMaxTimeShippingOrderStatus() {
        orderStatusCustomRepository.proceedShippingToConfirmedAfterTwoDays();
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void scheduleMaxTimeWaitingForPaymentOrderStatus() {
        orderStatusCustomRepository.proceedWaitingForPaymentToCancelledAfterOneHours();
    }
}
