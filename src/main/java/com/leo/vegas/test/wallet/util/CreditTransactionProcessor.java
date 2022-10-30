package com.leo.vegas.test.wallet.util;

import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.TransactionException;

import java.math.BigDecimal;

public class CreditTransactionProcessor implements TransactionProcessor{

    private CreditTransactionProcessor() {

    }

    private static class CreditTransactionProcessorHolder{
        private static final CreditTransactionProcessor INSTANCE = new CreditTransactionProcessor();
    }

    public static CreditTransactionProcessor getInstance() {
        return CreditTransactionProcessorHolder.INSTANCE;
    }

    @Override
    public BigDecimal process(BigDecimal currentBalance, BigDecimal transactionAmount) {
        if(currentBalance != null && transactionAmount != null) {
            return currentBalance.add(transactionAmount);
        }
        throw new TransactionException(ErrorCodes.BALANCE_OR_TRANSACTION_AMOUNT_CAN_NOT_BE_NULL);
    }
}
