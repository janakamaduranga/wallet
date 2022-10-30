package com.leo.vegas.test.wallet.service.impl;

import com.leo.vegas.test.wallet.dto.TransactionDto;
import com.leo.vegas.test.wallet.entity.PlayerAccount;
import com.leo.vegas.test.wallet.entity.Transaction;
import com.leo.vegas.test.wallet.exception.TransactionException;
import com.leo.vegas.test.wallet.repository.PlayerAccountRepository;
import com.leo.vegas.test.wallet.repository.TransactionRepository;
import com.leo.vegas.test.wallet.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    PlayerAccountRepository playerAccountRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    JdbcLockRegistry lockRegistry;

    @Mock
    ModelMapper modelMapper;

    @Mock
    Lock lock;

    TransactionDto creditTransactionDto ;
    TransactionDto debitTransactionDto;
    PlayerAccount creditPlayerAccount;
    PlayerAccount debitPlayerAccount;


    final String DEBIT_TX_ID = "debit123";
    final String CREDIT_TX_ID = "credit123";
    final String DEBIT_ACC_ID = "debitAccount123";
    final String CREDIT_ACCOUNT_ID = "creditAccount123";
    final BigDecimal DEBIT_AMOUNT = new BigDecimal("230.00");
    final BigDecimal CREDIT_AMOUNT = new BigDecimal("430.00");
    final BigDecimal PLAYER_ACCOUNT_BALANCE = new BigDecimal("2500.00");

    WalletServiceImpl walletService ;

    @Captor
    ArgumentCaptor<String> lockIdArgumentCaptor;

    @Captor
    ArgumentCaptor<PlayerAccount> playerAccountArgumentCaptor;

    @BeforeEach
    public void setUp() {
        debitTransactionDto = new TransactionDto();
        debitTransactionDto.setTransactionType(TransactionType.DEBIT);
        debitTransactionDto.setId(DEBIT_TX_ID);
        debitTransactionDto.setUserTransactionTime(new Date());
        debitTransactionDto.setAmount(DEBIT_AMOUNT);
        debitTransactionDto.setAccountId(DEBIT_ACC_ID);

        creditTransactionDto = new TransactionDto();
        creditTransactionDto.setTransactionType(TransactionType.CREDIT);
        creditTransactionDto.setId(CREDIT_TX_ID);
        creditTransactionDto.setUserTransactionTime(new Date());
        creditTransactionDto.setAccountId(CREDIT_ACCOUNT_ID);
        creditTransactionDto.setAmount(CREDIT_AMOUNT);

        creditPlayerAccount = new PlayerAccount();
        creditPlayerAccount.setBalance(PLAYER_ACCOUNT_BALANCE);
        creditPlayerAccount.setAccountId(CREDIT_ACCOUNT_ID);

        debitPlayerAccount = new PlayerAccount();
        debitPlayerAccount.setAccountId(DEBIT_ACC_ID);
        debitPlayerAccount.setBalance(PLAYER_ACCOUNT_BALANCE);

        walletService = new WalletServiceImpl(playerAccountRepository, transactionRepository,
                lockRegistry, modelMapper);
    }

    @Test
    void performCreditTransactionWhenValidInputThenSuccess() throws InterruptedException {
        Transaction transaction = new Transaction();
        when(transactionRepository.findById(CREDIT_TX_ID)).thenReturn(Optional.empty());
        when(playerAccountRepository.findById(CREDIT_ACCOUNT_ID)).thenReturn(Optional.of(creditPlayerAccount));
        when(playerAccountRepository.save(creditPlayerAccount)).thenReturn(creditPlayerAccount);
        when(modelMapper.map(creditTransactionDto, Transaction.class)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(lockRegistry.obtain(anyString())).thenReturn(lock);
        when(lock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        walletService.performTransaction(creditTransactionDto);

        verify(lockRegistry).obtain(lockIdArgumentCaptor.capture());
        verify(playerAccountRepository).save(playerAccountArgumentCaptor.capture());

        assertEquals(CREDIT_ACCOUNT_ID, lockIdArgumentCaptor.getValue());
        assertEquals(new BigDecimal("2930.00"), playerAccountArgumentCaptor.getValue().getBalance());
    }

    @Test
    void performCreditTransactionWhenTxIdExistInDbThenException() {
        Transaction transaction = new Transaction();
        when(transactionRepository.findById(CREDIT_TX_ID)).thenReturn(Optional.of(transaction));

        TransactionException transactionException = assertThrows(TransactionException.class,
                () -> walletService.performTransaction(creditTransactionDto));
        assertEquals("Transaction id already exist", transactionException.getMessage());

        verify(transactionRepository).findById(CREDIT_TX_ID);
    }

    @Test
    void performCreditTransactionWhenPlayerAccountNotFoundDbThenException() {
        when(transactionRepository.findById(CREDIT_TX_ID)).thenReturn(Optional.empty());
        when(playerAccountRepository.findById(CREDIT_ACCOUNT_ID)).thenReturn(Optional.empty());

        TransactionException transactionException = assertThrows(TransactionException.class,
                () -> walletService.performTransaction(creditTransactionDto));
        assertEquals("Player account not found", transactionException.getMessage());

        verify(transactionRepository).findById(CREDIT_TX_ID);
        verify(playerAccountRepository).findById(CREDIT_ACCOUNT_ID);
    }

    @Test
    void performCreditTransactionWhenLockAcquiringFailThenException() throws InterruptedException {
        when(transactionRepository.findById(CREDIT_TX_ID)).thenReturn(Optional.empty());
        when(playerAccountRepository.findById(CREDIT_ACCOUNT_ID)).thenReturn(Optional.of(creditPlayerAccount));
        when(lockRegistry.obtain(anyString())).thenReturn(lock);
        when(lock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        TransactionException transactionException = assertThrows(TransactionException.class,
                () -> walletService.performTransaction(creditTransactionDto));
        assertEquals("Concurrent modification", transactionException.getMessage());

        verify(transactionRepository).findById(CREDIT_TX_ID);
        verify(playerAccountRepository).findById(CREDIT_ACCOUNT_ID);
        verify(lock).tryLock(1, TimeUnit.SECONDS);
    }

    @Test
    void performCreditTransactionWhenLockObtainFailThenException() {
        when(transactionRepository.findById(CREDIT_TX_ID)).thenReturn(Optional.empty());
        when(playerAccountRepository.findById(CREDIT_ACCOUNT_ID)).thenReturn(Optional.of(creditPlayerAccount));
        when(lockRegistry.obtain(anyString())).thenThrow(new RuntimeException());

        TransactionException transactionException = assertThrows(TransactionException.class,
                () -> walletService.performTransaction(creditTransactionDto));
        assertEquals("Concurrent modification", transactionException.getMessage());

        verify(transactionRepository).findById(CREDIT_TX_ID);
        verify(playerAccountRepository).findById(CREDIT_ACCOUNT_ID);
        verify(lockRegistry).obtain(anyString());
    }

    @Test
    void performDebitTransactionWhenValidInputThenSuccess() throws InterruptedException {
        Transaction transaction = new Transaction();
        when(transactionRepository.findById(DEBIT_TX_ID)).thenReturn(Optional.empty());
        when(playerAccountRepository.findById(DEBIT_ACC_ID)).thenReturn(Optional.of(debitPlayerAccount));
        when(playerAccountRepository.save(debitPlayerAccount)).thenReturn(debitPlayerAccount);
        when(modelMapper.map(debitTransactionDto, Transaction.class)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(lockRegistry.obtain(anyString())).thenReturn(lock);
        when(lock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        walletService.performTransaction(debitTransactionDto);

        verify(lockRegistry).obtain(lockIdArgumentCaptor.capture());
        verify(playerAccountRepository).save(playerAccountArgumentCaptor.capture());

        assertEquals(DEBIT_ACC_ID, lockIdArgumentCaptor.getValue());
        assertEquals(new BigDecimal("2270.00"), playerAccountArgumentCaptor.getValue().getBalance());
    }
}
