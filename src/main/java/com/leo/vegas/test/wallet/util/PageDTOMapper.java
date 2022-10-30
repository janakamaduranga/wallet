package com.leo.vegas.test.wallet.util;


import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageDTOMapper{

    private PageDTOMapper() {

    }

    public static <T,R extends Serializable> PageDTO<R> getPageDTO(Function<? super T, ? extends R> function,
                                                                   Page<T> page) {
        PageDTO<R> pageDTO = new PageDTO<>();
        pageDTO.setData(page.getContent().stream().map(function)
                .collect(Collectors.toList()));
        pageDTO.setCurrentPage(page.getNumber());
        pageDTO.setTotalPages(page.getTotalPages());
        pageDTO.setTotalItems(page.getTotalElements());
        pageDTO.setFirst(page.isFirst());
        pageDTO.setLast(page.isLast());

        return pageDTO;
    }
}