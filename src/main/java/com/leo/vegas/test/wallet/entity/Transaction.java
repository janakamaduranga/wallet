package com.leo.vegas.test.wallet.entity;

import com.leo.vegas.test.wallet.util.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class Transaction extends BaseEntity{
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "user_transaction_time")
    private Date userTransactionTime;

    @Column(name = "amount")
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "player_account_id")
    private PlayerAccount playerAccount;
}
