package com.leo.vegas.test.wallet.exception;

public class UserException extends RuntimeException{

    private final ErrorCodes errorCodes;

    public UserException(Exception e, ErrorCodes errorCodes) {
        super(errorCodes.getDescription(), e);
        this.errorCodes = errorCodes;
    }

    public UserException(ErrorCodes errorCodes) {
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
