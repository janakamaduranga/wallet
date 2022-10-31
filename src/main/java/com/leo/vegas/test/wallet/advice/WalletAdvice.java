package com.leo.vegas.test.wallet.advice;

import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.TransactionException;
import com.leo.vegas.test.wallet.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@Slf4j
@ControllerAdvice(basePackages = "com.leo.vegas.test.wallet.controller")
public class WalletAdvice {

    private static final String ERROR = "Error:";

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleArgumentMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error(ERROR, ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ERROR, ex);
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        return new ResponseEntity<>(processFieldErrors(fieldErrors), (HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleMConstraintViolationException(ConstraintViolationException ex) {
        log.error(ERROR, ex);
        return new ResponseEntity<>(buildValidationErrors(ex.getConstraintViolations()), (HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(TransactionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleTransactionException(TransactionException ex) {
        log.error(ERROR, ex);
        return new ResponseEntity<>(ex.getErrorDetails(), getStatusCode(ex.getErrorCode()));
    }

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleUserException(UserException ex) {
        log.error(ERROR, ex);
        return new ResponseEntity<>(ex.getErrorDetails(), getStatusCodeForUserException(ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleInternalError(Exception ex) {
        log.error(ERROR, ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private HttpStatus getStatusCode(int errorCode) {
        if (errorCode == ErrorCodes.PLAYER_ACCOUNT_NOT_FOUND.getId() ||
                errorCode == ErrorCodes.TRANSACTION_ID_NOT_UNIQUE.getId()
        ) {
            return HttpStatus.BAD_REQUEST;
        } else if (errorCode == ErrorCodes.CONCURRENT_MODIFICATION.getId() ||
                errorCode == ErrorCodes.BALANCE_OR_TRANSACTION_AMOUNT_CAN_NOT_BE_NULL.getId() ||
                errorCode == ErrorCodes.INSUFFICIENT_BALANCE.getId() ||
                errorCode == ErrorCodes.ACCOUNT_ALREADY_EXIST.getId()
        ) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private HttpStatus getStatusCodeForUserException(int errorCode) {
        if (errorCode == ErrorCodes.PLAYER_ACCOUNT_NOT_FOUND.getId()) {
            return HttpStatus.NO_CONTENT;
        } else if (errorCode == ErrorCodes.ACCOUNT_ALREADY_EXIST.getId()
        ) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String processFieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
        StringBuilder errorBuilder = new StringBuilder();
        for (org.springframework.validation.FieldError fieldError : fieldErrors) {
            if (errorBuilder.length() > 0) {
                errorBuilder.append(",");
            }
            errorBuilder.append(fieldError.getField()).append(" : ")
                    .append(fieldError.getDefaultMessage());
        }
        return errorBuilder.toString();
    }

    private String buildValidationErrors(
            Set<ConstraintViolation<?>> violations) {
        StringBuilder errorBuilder = new StringBuilder();

        for (ConstraintViolation<?> constraintViolation : violations) {
            if (errorBuilder.length() > 0) {
                errorBuilder.append(",");
            }
            errorBuilder.append(constraintViolation.getMessage());
        }
        return errorBuilder.toString();
    }
}
