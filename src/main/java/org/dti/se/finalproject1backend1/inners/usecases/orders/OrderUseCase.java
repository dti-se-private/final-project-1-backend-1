package org.dti.se.finalproject1backend1.inners.usecases.orders;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.OrderCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderUseCase {
    @Autowired
    OrderCustomRepository orderCustomRepository;
    @Autowired
    AccountRepository accountRepository;

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
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        return orderCustomRepository
                .getOrders(page, size, filters, search);
    }


    public OrderResponse getOrder(UUID orderId) {
        return orderCustomRepository
                .getOrder(orderId);
    }

}
