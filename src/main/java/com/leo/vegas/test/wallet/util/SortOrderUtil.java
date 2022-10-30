package com.leo.vegas.test.wallet.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SortOrderUtil {

    private static final String DESCENDING = "-";

    private SortOrderUtil() {
    }

    public static List<Sort.Order> getSortOrders(String orderBy) {
        if (Objects.nonNull(orderBy) && orderBy.trim()
                .length() > 0) {
            List<Sort.Order> sortOrder = new ArrayList<>();
            String[] orderByList = orderBy.split(",");
            for (String field : orderByList) {
                if (field.contains(DESCENDING)) {
                    sortOrder.add(new Sort.Order(Sort.Direction.DESC, field.split(DESCENDING)[1]));
                } else {
                    sortOrder.add(new Sort.Order(Sort.Direction.ASC, field));
                }
            }
            return sortOrder;
        }
        return new ArrayList<>();
    }
}
