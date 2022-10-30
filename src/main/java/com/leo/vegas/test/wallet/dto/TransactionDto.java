package com.leo.vegas.test.wallet.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leo.vegas.test.wallet.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto implements Serializable {
    @NotBlank
    private String id;

    @JsonIgnore
    private String accountId;

    @JsonIgnore
    private TransactionType transactionType;

    @NotNull
    private Date userTransactionTime;

    //need to clarify allowed maximum values
    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    private BigDecimal amount;
}
