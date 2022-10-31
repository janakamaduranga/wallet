package com.leo.vegas.test.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.exception.ErrorCodes;
import com.leo.vegas.test.wallet.exception.UserException;
import com.leo.vegas.test.wallet.security.AuthEntryPointWallet;
import com.leo.vegas.test.wallet.security.WebSecurityConfig;
import com.leo.vegas.test.wallet.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@Import({WebSecurityConfig.class})
@WebMvcTest(value = PlayerAccountController.class)
class PlayerAccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountService accountService;

    @MockBean
    AuthEntryPointWallet authEntryPointWallet;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    ArgumentCaptor<PlayerAccountDto> playerAccountDtoArgumentCaptor;

    @Captor
    ArgumentCaptor<String> accountIdArgumentCaptor;

    PlayerAccountDto playerAccountDto;

    final String PLAYER_ACCOUNT_ID = "user345";
    final BigDecimal BALANCE = new BigDecimal("3400");
    final String CREATE_USER_ACCOUNT = "/v1/accounts";
    final String READ_USER_ACCOUNT = "/v1/accounts/{accountId}";


    @BeforeEach
    public void setup() {
        playerAccountDto = new PlayerAccountDto();
        playerAccountDto.setAccountId(PLAYER_ACCOUNT_ID);
        playerAccountDto.setBalance(BALANCE);
    }

    @Test
    void createAccountWhenValidInputThenStatusCreated() throws Exception {
        when(accountService.createAccount(any(PlayerAccountDto.class))).thenReturn(playerAccountDto);
        String result = mockMvc.perform(post(CREATE_USER_ACCOUNT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(playerAccountDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PlayerAccountDto response = objectMapper.readValue(result, PlayerAccountDto.class);

        verify(accountService).createAccount(playerAccountDtoArgumentCaptor.capture());
        assertEquals(PLAYER_ACCOUNT_ID, playerAccountDtoArgumentCaptor.getValue().getAccountId());
        assertEquals(PLAYER_ACCOUNT_ID, response.getAccountId());
    }

    @Test
    void createAccountWhenInValidInputThenStatusBadRequest() throws Exception {
        playerAccountDto.setBalance(null);
        mockMvc.perform(post(CREATE_USER_ACCOUNT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(playerAccountDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void createAccountWhenAccountIdExistInDbThenStatusConflict() throws Exception {
        when(accountService.createAccount(any(PlayerAccountDto.class))).thenThrow(new UserException(ErrorCodes.ACCOUNT_ALREADY_EXIST));
        mockMvc.perform(post(CREATE_USER_ACCOUNT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer jj")
                        .content(objectMapper.writeValueAsString(playerAccountDto)))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void getAccountByIdWhenValidInputThenStatusOk() throws Exception {
        when(accountService.getAccount(PLAYER_ACCOUNT_ID)).thenReturn(playerAccountDto);
        String result = mockMvc.perform(get(READ_USER_ACCOUNT, PLAYER_ACCOUNT_ID)
                        .header("Authorization", "Bearer jj")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PlayerAccountDto response = objectMapper.readValue(result, PlayerAccountDto.class);

        verify(accountService).getAccount(accountIdArgumentCaptor.capture());
        assertEquals(PLAYER_ACCOUNT_ID, accountIdArgumentCaptor.getValue());
        assertEquals(PLAYER_ACCOUNT_ID, response.getAccountId());
    }

    @Test
    void getAccountByIdWhenAccountNotFoundThenStatusNoContent() throws Exception {
        when(accountService.getAccount(PLAYER_ACCOUNT_ID)).thenThrow(new UserException(ErrorCodes.PLAYER_ACCOUNT_NOT_FOUND));
        mockMvc.perform(get(READ_USER_ACCOUNT, PLAYER_ACCOUNT_ID)
                        .header("Authorization", "Bearer jj")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getContentAsString();

        verify(accountService).getAccount(accountIdArgumentCaptor.capture());
        assertEquals(PLAYER_ACCOUNT_ID, accountIdArgumentCaptor.getValue());
    }
}
