package org.dti.se.finalproject1backend1.outers.deliveries.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TransactionWebFilterImpl extends OncePerRequestFilter {

    @Autowired
    @Qualifier("oneTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            filterChain.doFilter(request, response);
            transactionManager.commit(status);
        } catch (Exception exception) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw exception;
        }
    }
}