package com.leo.vegas.test.wallet.service;

import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.dto.TransactionDto;
import com.leo.vegas.test.wallet.util.PageDTO;

public interface WalletService {

    PlayerAccountDto performTransaction(TransactionDto transactionDto);
    PageDTO<TransactionDto> findTransactionsByAccountId(String accountId,
                                                        int page,
                                                        int size,
                                                        String sort);
}
