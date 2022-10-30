package com.leo.vegas.test.wallet.util;

import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.TransactionException;

import java.math.BigDecimal;

public class DebitTransactionProcessor implements TransactionProcessor{

    private DebitTransactionProcessor() {

    }

    private static class DebitTransactionProcessorHolder{
        private static final DebitTransactionProcessor INSTANCE = new DebitTransactionProcessor();
    }

    public static DebitTransactionProcessor getInstance() {
        return DebitTransactionProcessorHolder.INSTANCE;
    }

    @Override
    public BigDecimal process(BigDecimal currentBalance, BigDecimal transactionAmount) {
        if(currentBalance != null && transactionAmount != null) {
            if (currentBalance.compareTo(BigDecimal.ZERO) == 0) {
                throw new TransactionException(ErrorCodes.INSUFFICIENT_BALANCE);
            }
            return currentBalance.subtract(transactionAmount);
        }
        throw new TransactionException(ErrorCodes.BALANCE_OR_TRANSACTION_AMOUNT_CAN_NOT_BE_NULL);
    }
}
