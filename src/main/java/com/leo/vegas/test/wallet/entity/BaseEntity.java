package com.leo.vegas.test.wallet.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public class BaseEntity implements Serializable {
    @Column(name = "version")
    @Version
    private Integer version;

    //should be ZonedDateTime, but H2 does not support it
    @Column(name = "createdAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

}
