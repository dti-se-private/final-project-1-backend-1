package org.dti.se.finalproject1backend1.outers.deliveries.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
public class TransactionWebFilterImpl extends GenericFilterBean {

    @Autowired
    @Qualifier("oneTransactionManager")
    private PlatformTransactionManager transactionManager;

    private static final Logger logger = LoggerFactory.getLogger(TransactionWebFilterImpl.class);

    private static final Long MAX_RETRIES = 3L;
    private static final Long RETRY_DELAY_MS = 100L;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!((request instanceof HttpServletRequest httpRequest) && (response instanceof HttpServletResponse httpResponse))) {
            throw new ServletException("OncePerRequestFilter only supports HTTP requests");
        }

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);

        Long retryCount = 0L;
        while (retryCount <= MAX_RETRIES) {
            TransactionStatus status = transactionManager.getTransaction(definition);

            try {
                chain.doFilter(request, response);
                transactionManager.commit(status);
                return; // Success, exit.
            } catch (ServletException | IOException servletException) {
                if (!status.isCompleted()) {
                    transactionManager.rollback(status);
                }
                throw servletException;
            } catch (ConcurrencyFailureException concurrencyException) {
                retryCount++;
                logger.warn("Concurrency exception (retry {}/{}), retrying: {}", retryCount, MAX_RETRIES, concurrencyException.getMessage());
                if (!status.isCompleted()) {
                    transactionManager.rollback(status);
                }
                if (retryCount <= MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retryCount);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Retry delay interrupted", e);
                        break; // Exit retry loop if interrupted.
                    }
                } else {
                    logger.error("Max retries ({}) reached for concurrency exception, failing.", MAX_RETRIES);
                    httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return; // Exit after max retries.
                }
            } catch (Exception otherException) {
                // For other exceptions, rollback and fail without retry.
                otherException.printStackTrace();
                logger.error("Non-concurrency exception, rolling back and failing: {}", otherException.getMessage(), otherException);
                if (!status.isCompleted()) {
                    transactionManager.rollback(status);
                }
                break; // Exit without retry.
            }
        }
        // Should not reach here in normal flow, but as a fallback.
        httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }


}