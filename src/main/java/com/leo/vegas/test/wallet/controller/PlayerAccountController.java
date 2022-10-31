package com.leo.vegas.test.wallet.controller;

import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/v1")
@Validated
public class PlayerAccountController {

    private final AccountService accountService;

    public PlayerAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create a new user account", tags = { "playerAccountDto" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successfully created",
                    content = @Content(schema = @Schema(implementation = PlayerAccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "User already exist"),
            @ApiResponse(responseCode = "500", description = "Internal error") })
    @PostMapping(path = "/accounts", consumes = { "application/json"})
    public ResponseEntity<PlayerAccountDto> createAccount( @Valid @RequestBody PlayerAccountDto playerAccountDto) {
        return new ResponseEntity<>(accountService.createAccount(playerAccountDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get user account by account Id", tags = { "accountId" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfully executed",
                    content = @Content(schema = @Schema(implementation = PlayerAccountDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "204", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal error") })
    @GetMapping(path = "/accounts/{accountId}")
    public ResponseEntity<PlayerAccountDto> getAccountById(@PathVariable(name = "accountId") String accountId) {
        return new ResponseEntity<>(accountService.getAccount(accountId), HttpStatus.OK);
    }
}
