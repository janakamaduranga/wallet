package com.leo.vegas.test.wallet.util;

import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.TransactionException;

public class TransactionProcessorFactory {
    private TransactionProcessorFactory() {

    }

    public static TransactionProcessor getTransactionProcessorByTransaction(
            TransactionType transactionType) {
        if(TransactionType.CREDIT == transactionType) {
            return CreditTransactionProcessor.getInstance();
        } else if(TransactionType.DEBIT == transactionType) {
            return DebitTransactionProcessor.getInstance();
        }
        throw new TransactionException(ErrorCodes.INVALID_TX_TYPE);
    }
}
