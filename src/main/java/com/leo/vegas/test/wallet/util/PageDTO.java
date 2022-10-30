package com.leo.vegas.test.wallet.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PageDTO<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean isFirst;
    private boolean isLast;
    List<T> data;
}
