package com.leo.vegas.test.wallet.service.impl;

import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.dto.TransactionDto;
import com.leo.vegas.test.wallet.entity.PlayerAccount;
import com.leo.vegas.test.wallet.entity.Transaction;
import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.TransactionException;
import com.leo.vegas.test.wallet.repository.PlayerAccountRepository;
import com.leo.vegas.test.wallet.repository.TransactionRepository;
import com.leo.vegas.test.wallet.service.WalletService;
import com.leo.vegas.test.wallet.util.PageDTO;
import com.leo.vegas.test.wallet.util.PageDTOMapper;
import com.leo.vegas.test.wallet.util.SortOrderUtil;
import com.leo.vegas.test.wallet.util.TransactionProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    private final PlayerAccountRepository playerAccountRepository;
    private final TransactionRepository transactionRepository;
    private final JdbcLockRegistry lockRegistry;

    private final ModelMapper modelMapper;

    public WalletServiceImpl(PlayerAccountRepository playerAccountRepository,
                         TransactionRepository transactionRepository,
                             JdbcLockRegistry lockRegistry, ModelMapper modelMapper) {
        this.playerAccountRepository = playerAccountRepository;
        this.transactionRepository = transactionRepository;
        this.lockRegistry = lockRegistry;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PlayerAccountDto performTransaction(TransactionDto transactionDto) {
        /*
          if the transaction id already exist, can not proceed further
         */
        if(transactionRepository.findById(transactionDto.getId()).isPresent()) {
            log.info("Transaction id already exist in db : {} ", transactionDto.getId());
            throw new TransactionException(ErrorCodes.TRANSACTION_ID_NOT_UNIQUE);
        }

        /*
          if there is no record in the db for the given account id,
          can not proceed
         */
       PlayerAccount playerAccount = playerAccountRepository.findById(transactionDto.getAccountId())
               .orElseThrow(() -> new TransactionException(ErrorCodes.PLAYER_ACCOUNT_NOT_FOUND));
        boolean lockAcquired = false;
        Lock lock = null;
        try{
            /*
              acquire a distributed lock to stop concurrent modifications
             */
            lock = lockRegistry.obtain(playerAccount.getAccountId());
            //value can be read from a property if needed
            lockAcquired = lock.tryLock(1, TimeUnit.SECONDS);
            if(lockAcquired) {
                BigDecimal newBalance = TransactionProcessorFactory.getTransactionProcessorByTransaction(transactionDto.getTransactionType())
                                .process(playerAccount.getBalance(), transactionDto.getAmount());
                playerAccount.setBalance(newBalance);
                playerAccount.setUpdatedAt(new Date());

                playerAccount = playerAccountRepository.save(playerAccount);

                Transaction transaction = modelMapper.map(transactionDto, Transaction.class);
                transaction.setPlayerAccount(playerAccount);
                transaction.setCreatedAt(new Date());
                transactionRepository.save(transaction);
            } else{
                throw new TransactionException(ErrorCodes.CONCURRENT_MODIFICATION);
            }

        } catch(Exception e) {
            throw new TransactionException(e, ErrorCodes.CONCURRENT_MODIFICATION);
        } finally {
            if(lockAcquired) {
                lock.unlock();
            }
        }
        return modelMapper.map(playerAccount, PlayerAccountDto.class);
    }

    public PageDTO<TransactionDto> findTransactionsByAccountId(String accountId,
                                                        int page,
                                                        int size,
                                                        String sort) {
        List<Sort.Order> sortOrder = SortOrderUtil.getSortOrders(sort);
        Page<Transaction> transactions = transactionRepository.findByPlayerAccountAccountId(accountId, PageRequest.of(page, size, Sort.by(sortOrder)));
        return PageDTOMapper.getPageDTO(transaction ->
                modelMapper.map(transaction, TransactionDto.class), transactions);
    }
}
