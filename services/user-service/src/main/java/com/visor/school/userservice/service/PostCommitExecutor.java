package com.visor.school.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Executes side effects after successful transaction commit.
 */
@Component
public class PostCommitExecutor {

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 100L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void runAfterCommit(String operationName, Runnable operation) {
        if (!TransactionSynchronizationManager.isSynchronizationActive() ||
            !TransactionSynchronizationManager.isActualTransactionActive()) {
            executeNow(operationName, operation);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executeNow(operationName, operation);
            }
        });
    }

    private void executeNow(String operationName, Runnable operation) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                operation.run();
                return;
            } catch (RuntimeException ex) {
                lastException = ex;
                logger.warn("Post-commit operation '{}' failed on attempt {}/{}", operationName, attempt, MAX_ATTEMPTS, ex);
                if (attempt < MAX_ATTEMPTS) {
                    sleep(INITIAL_DELAY_MS * attempt);
                }
            }
        }
        if (lastException != null) {
            throw new IllegalStateException("Post-commit operation failed: " + operationName, lastException);
        }
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
