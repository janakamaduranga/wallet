package com.leo.vegas.test.wallet.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "player_account")
@Getter
@Setter
@NoArgsConstructor
public class PlayerAccount extends BaseEntity{

    @Id
    @Column(name = "account_id")
    private String accountId;

    /**should be ZonedDateTime, but H2 does not support it,
     * in production environment, both timestamp or time zone
     * can be saved if the db does not support
     *
     */
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "balance")
    private BigDecimal balance;

    @OneToMany(cascade = CascadeType.REFRESH,
    fetch = FetchType.LAZY, mappedBy = "playerAccount")
    private List<Transaction> transactions;

}
