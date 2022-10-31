package com.leo.vegas.test.wallet.service;

import com.leo.vegas.test.wallet.dto.PlayerAccountDto;

public interface AccountService {
    PlayerAccountDto createAccount(PlayerAccountDto playerAccountDto);
    PlayerAccountDto getAccount(String accountId);
}
