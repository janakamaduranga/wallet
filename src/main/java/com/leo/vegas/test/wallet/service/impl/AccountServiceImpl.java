package com.leo.vegas.test.wallet.service.impl;

import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.entity.PlayerAccount;
import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.UserException;
import com.leo.vegas.test.wallet.repository.PlayerAccountRepository;
import com.leo.vegas.test.wallet.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AccountServiceImpl implements AccountService {
    private final PlayerAccountRepository playerAccountRepository;
    private final ModelMapper modelMapper;

    public AccountServiceImpl(PlayerAccountRepository playerAccountRepository,
                              ModelMapper modelMapper) {
        this.playerAccountRepository = playerAccountRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PlayerAccountDto createAccount(PlayerAccountDto playerAccountDto) {
        if(playerAccountRepository.findById(playerAccountDto.getAccountId()).isPresent()) {
            throw new UserException(ErrorCodes.ACCOUNT_ALREADY_EXIST);
        }
        PlayerAccount playerAccount = modelMapper.map(playerAccountDto, PlayerAccount.class);
        playerAccount.setUpdatedAt(new Date());
        playerAccount.setCreatedAt(new Date());

        return modelMapper.map(playerAccountRepository.save(playerAccount), PlayerAccountDto.class);
    }

    @Override
    public PlayerAccountDto getAccount(String accountId) {
        PlayerAccount playerAccount = playerAccountRepository.findById(accountId)
                .orElseThrow(() -> new UserException(ErrorCodes.PLAYER_ACCOUNT_NOT_FOUND));
        return modelMapper.map(playerAccount, PlayerAccountDto.class);
    }
}
