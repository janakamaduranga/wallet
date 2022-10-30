package com.leo.vegas.test.wallet.exception;

public class TransactionException extends RuntimeException{

    private final ErrorCodes errorCodes;

    public TransactionException(Exception e, ErrorCodes errorCodes) {
        super(errorCodes.getDescription(), e);
        this.errorCodes = errorCodes;
    }

    public TransactionException(ErrorCodes errorCodes) {
        super(errorCodes.getDescription());
        this.errorCodes = errorCodes;
    }

    public String getErrorDetails() {
        return getMessage() ;
    }

    public int getErrorCode() {
        return errorCodes.getId();
    }
}
