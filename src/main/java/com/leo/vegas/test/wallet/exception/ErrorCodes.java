package com.leo.vegas.test.wallet.exception;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    PLAYER_ACCOUNT_NOT_FOUND(300, "Player account not found"),
    TRANSACTION_ID_NOT_UNIQUE(301, "Transaction id already exist"),
    CONCURRENT_MODIFICATION(302, "Concurrent modification"),
    BALANCE_OR_TRANSACTION_AMOUNT_CAN_NOT_BE_NULL(303,
            "Balance or transaction amount can not be null"),
    INSUFFICIENT_BALANCE(304, "Insufficient balance"),
    INVALID_TX_TYPE(305, "Invalid transaction type"),
    ACCOUNT_ALREADY_EXIST(306, "Account already exist");

    private final int id;
    private final String description;

    ErrorCodes(int id, String description) {
        this.id = id;
        this.description = description;
    }
}
