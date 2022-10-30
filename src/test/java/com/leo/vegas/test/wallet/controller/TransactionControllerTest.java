package com.leo.vegas.test.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.dto.TransactionDto;
import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.TransactionException;
import com.leo.vegas.test.wallet.security.AuthEntryPointWallet;
import com.leo.vegas.test.wallet.security.WebSecurityConfig;
import com.leo.vegas.test.wallet.service.WalletService;
import com.leo.vegas.test.wallet.util.PageDTO;
import com.leo.vegas.test.wallet.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@Import({WebSecurityConfig.class})
@WebMvcTest(value = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WalletService walletService;

    @MockBean
    AuthEntryPointWallet authEntryPointWallet;

    @Mock
    PageDTO<TransactionDto> pageDTO;

    @Autowired
    private ObjectMapper objectMapper;

    static TransactionDto creditTransactionDto ;
    static TransactionDto debitTransactionDto;
    static final String DEBIT_TX_ID = "debit123";
    static final String CREDIT_TX_ID = "credit123";
    static final String DEBIT_ACC_ID = "debitAccount123";
    static final String CREDIT_ACCOUNT_ID = "creditAccount123";
    static final BigDecimal DEBIT_AMOUNT = new BigDecimal("230.00");
    static final BigDecimal CREDIT_AMOUNT = new BigDecimal("430.00");

    @Captor
    private ArgumentCaptor<TransactionDto> transactionDtoArgumentCaptor;

    @Captor
    ArgumentCaptor<String> accountIdCaptor;

    @Captor
    ArgumentCaptor<Integer> pageCaptor;

    @Captor
    ArgumentCaptor<Integer> sizeCaptor;

    @Captor
    ArgumentCaptor<String> sortCaptor;

    final String DEBIT_URL = "/v1/accounts/{accountId}/transactions/debits";
    final String CREDIT_URL = "/v1/accounts/{accountId}/transactions/credits";
    final String TRANSACTION_HISTORY_URL = "/v1/accounts/{accountId}/transactions";


    @BeforeEach
    public void setup() {
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
    }

    @Test
    void debitAccountWhenInputValidThenStatusCreated() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(DEBIT_ACC_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));

        when(walletService.performTransaction(any(TransactionDto.class))).thenReturn(playerAccountDto);

        String result = mockMvc.perform(post(DEBIT_URL, DEBIT_ACC_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(debitTransactionDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        verify(walletService).performTransaction(transactionDtoArgumentCaptor.capture());
        assertEquals(TransactionType.DEBIT, transactionDtoArgumentCaptor.getValue().getTransactionType());
        assertEquals(DEBIT_ACC_ID, playerAccountDto.getAccountId());
    }

    @Test
    void debitAccountWhenJwtMissingThenStatusUnauthorized() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(DEBIT_ACC_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));

        when(walletService.performTransaction(any(TransactionDto.class))).thenReturn(playerAccountDto);

        mockMvc.perform(post(DEBIT_URL, DEBIT_ACC_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(debitTransactionDto)))
                .andExpect(status().isUnauthorized());

        verify(walletService, times(0)).performTransaction(transactionDtoArgumentCaptor.capture());
    }

    @Test
    void debitAccountWhenInvalidInputThenStatusBadRequest() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(DEBIT_ACC_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));
        debitTransactionDto.setAmount(null);

        when(walletService.performTransaction(any(TransactionDto.class))).thenReturn(playerAccountDto);

        mockMvc.perform(post(DEBIT_URL, DEBIT_ACC_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(debitTransactionDto)))
                .andExpect(status().isBadRequest());

        verify(walletService, times(0)).performTransaction(transactionDtoArgumentCaptor.capture());
    }

    @Test
    void debitAccountWhenConcurrentIssueThenStatusConflict() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(DEBIT_ACC_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));

        when(walletService.performTransaction(any(TransactionDto.class)))
                .thenThrow(new TransactionException(ErrorCodes.CONCURRENT_MODIFICATION));

        mockMvc.perform(post(DEBIT_URL, DEBIT_ACC_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(debitTransactionDto)))
                .andExpect(status().isConflict());

        verify(walletService).performTransaction(transactionDtoArgumentCaptor.capture());
        assertEquals(TransactionType.DEBIT, transactionDtoArgumentCaptor.getValue().getTransactionType());
        assertEquals(DEBIT_ACC_ID, playerAccountDto.getAccountId());
    }

    @Test
    void creditAccountWhenInputValidThenStatusCreated() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(CREDIT_ACCOUNT_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));

        when(walletService.performTransaction(any(TransactionDto.class))).thenReturn(playerAccountDto);

        String result = mockMvc.perform(post(CREDIT_URL, CREDIT_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(creditTransactionDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        verify(walletService).performTransaction(transactionDtoArgumentCaptor.capture());
        assertEquals(TransactionType.CREDIT, transactionDtoArgumentCaptor.getValue().getTransactionType());
        assertEquals(CREDIT_ACCOUNT_ID, playerAccountDto.getAccountId());
    }

    @Test
    void creditAccountWhenJwtMissingThenStatusUnauthorized() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(CREDIT_ACCOUNT_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));

        when(walletService.performTransaction(any(TransactionDto.class))).thenReturn(playerAccountDto);

        mockMvc.perform(post(CREDIT_URL, CREDIT_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(creditTransactionDto)))
                .andExpect(status().isUnauthorized());

        verify(walletService, times(0)).performTransaction(transactionDtoArgumentCaptor.capture());
    }

    @Test
    void creditAccountWhenInvalidInputThenStatusBadRequest() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(CREDIT_ACCOUNT_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));
        creditTransactionDto.setAmount(null);

        when(walletService.performTransaction(any(TransactionDto.class))).thenReturn(playerAccountDto);

        mockMvc.perform(post(CREDIT_URL, CREDIT_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(creditTransactionDto)))
                .andExpect(status().isBadRequest());

        verify(walletService, times(0)).performTransaction(transactionDtoArgumentCaptor.capture());
    }

    @Test
    void creditsAccountWhenConcurrentIssueThenStatusConflict() throws Exception {
        PlayerAccountDto playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(CREDIT_ACCOUNT_ID);
        playerAccountDto.setBalance(new BigDecimal("230.00"));

        when(walletService.performTransaction(any(TransactionDto.class)))
                .thenThrow(new TransactionException(ErrorCodes.CONCURRENT_MODIFICATION));

        mockMvc.perform(post(CREDIT_URL, CREDIT_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(creditTransactionDto)))
                .andExpect(status().isConflict());

        verify(walletService).performTransaction(transactionDtoArgumentCaptor.capture());
        assertEquals(TransactionType.CREDIT, transactionDtoArgumentCaptor.getValue().getTransactionType());
    }

    @Test
    void getTransactionByAccountIdWhenValidInputsThenStatusOk() throws Exception {
        final int PAGE = 0;
        final int SIZE = 3;
        final String SORT = "id";

        when(walletService.findTransactionsByAccountId(DEBIT_ACC_ID, PAGE, SIZE, SORT))
                .thenReturn(pageDTO);
        mockMvc.perform(get(TRANSACTION_HISTORY_URL, DEBIT_ACC_ID)
                        .header("Authorization", "Bearer jj")
                        .param("page", String.valueOf(PAGE))
                        .param("size", String.valueOf(SIZE))
                        .param("sort", SORT))
                .andExpect(status().isOk());

        verify(walletService).findTransactionsByAccountId(accountIdCaptor.capture(), pageCaptor.capture(),
                sizeCaptor.capture(), sortCaptor.capture());

        assertEquals(DEBIT_ACC_ID, accountIdCaptor.getValue());
        assertEquals(PAGE, pageCaptor.getValue());
        assertEquals(SIZE, sizeCaptor.getValue());
        assertEquals(SORT, sortCaptor.getValue());
    }

    @Test
    void getTransactionByAccountIdWhenTokenMissingThenStatusUnauthorized() throws Exception {
        final int PAGE = 0;
        final int SIZE = 3;
        final String SORT = "id";

        when(walletService.findTransactionsByAccountId(DEBIT_ACC_ID, PAGE, SIZE, SORT))
                .thenReturn(pageDTO);
        mockMvc.perform(get(TRANSACTION_HISTORY_URL, DEBIT_ACC_ID)
                        .param("page", String.valueOf(PAGE))
                        .param("size", String.valueOf(SIZE))
                        .param("sort", SORT))
                .andExpect(status().isUnauthorized());

        verify(walletService, times(0)).findTransactionsByAccountId(accountIdCaptor.capture(), pageCaptor.capture(),
                sizeCaptor.capture(), sortCaptor.capture());
    }

    @Test
    void getTransactionByAccountIdWhenInValidInputsThenStatusBadRequest() throws Exception {
        final int PAGE = -1;
        final int SIZE = 3;
        final String SORT = "id";

        when(walletService.findTransactionsByAccountId(DEBIT_ACC_ID, PAGE, SIZE, SORT))
                .thenReturn(pageDTO);
        mockMvc.perform(get(TRANSACTION_HISTORY_URL, DEBIT_ACC_ID)
                        .header("Authorization", "Bearer jj")
                        .param("page", String.valueOf(PAGE))
                        .param("size", String.valueOf(SIZE))
                        .param("sort", SORT))
                .andExpect(status().isBadRequest());
    }
}
