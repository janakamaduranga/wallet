package com.leo.vegas.test.wallet.controller;

import com.leo.vegas.test.wallet.dto.PlayerAccountDto;
import com.leo.vegas.test.wallet.dto.TransactionDto;
import com.leo.vegas.test.wallet.service.WalletService;
import com.leo.vegas.test.wallet.util.PageDTO;
import com.leo.vegas.test.wallet.util.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/v1")
@Validated
public class  TransactionController {

    private final WalletService walletService;
    public TransactionController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Add a new debit transaction", tags = { "transactionDto" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successfully debited",
                    content = @Content(schema = @Schema(implementation = PlayerAccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Conflicting operation"),
            @ApiResponse(responseCode = "500", description = "Internal error") })
    @PostMapping(path = "/accounts/{accountId}/transactions/debits", consumes = { "application/json"})
    public ResponseEntity<PlayerAccountDto> debitAccount(
            @PathVariable(name = "accountId") String accountId,
            @Parameter(description="TransactionDto to add. Cannot null or empty.",
                    required=true, schema=@Schema(implementation = TransactionDto.class))
            @Valid @RequestBody TransactionDto transactionDto) {

        transactionDto.setTransactionType(TransactionType.DEBIT);
        transactionDto.setAccountId(accountId);

        return new ResponseEntity<>(walletService.performTransaction(transactionDto),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Add a new credit transaction", tags = { "transactionDto" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successfully credited",
                    content = @Content(schema = @Schema(implementation = PlayerAccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Conflicting operation"),
            @ApiResponse(responseCode = "500", description = "Internal error") })
    @PostMapping(path = "/accounts/{accountId}/transactions/credits", consumes = { "application/json"})
    public ResponseEntity<PlayerAccountDto> creditAccount(
            @PathVariable(name = "accountId") String accountId,
            @Parameter(description="TransactionDto to add. Cannot null or empty.",
                    required=true, schema=@Schema(implementation = TransactionDto.class))
            @Valid @RequestBody TransactionDto transactionDto) {

        transactionDto.setTransactionType(TransactionType.CREDIT);
        transactionDto.setAccountId(accountId);

        return new ResponseEntity<>(walletService.performTransaction(transactionDto),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Return paginated transactions for given account id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful result",
                    content = @Content(schema = @Schema(implementation = PageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal error") })
    @GetMapping(path = "/accounts/{accountId}/transactions")
    public ResponseEntity<PageDTO<TransactionDto>> getTransactionByAccountId(@PathVariable(name = "accountId") String accountId,
                                                                             @Min(value = 0) @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "3") int size,
                                                                             @Parameter(name ="sort", schema = @Schema(description = "allow comma separated values, minus is used for descending order, eg:-id, total",type = "string", allowableValues = {"id", "amount"})) @RequestParam(defaultValue = "id") String sort) {
        return ResponseEntity.ok(walletService.findTransactionsByAccountId(accountId, page, size, sort));

    }
}
