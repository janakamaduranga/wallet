package com.leo.vegas.test.wallet.util;

import java.math.BigDecimal;

public interface TransactionProcessor {
    BigDecimal process(BigDecimal currentBalance, BigDecimal transactionAmount);
}
