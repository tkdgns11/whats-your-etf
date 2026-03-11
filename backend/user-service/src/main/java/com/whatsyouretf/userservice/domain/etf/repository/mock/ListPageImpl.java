package com.whatsyouretf.userservice.domain.etf.repository.mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPageImpl<T> {
    public Page<T> toPage(List<T> list, Pageable pageable) {

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());

        List<T> content = list.subList(start, end);

        return new PageImpl<>(content, pageable, list.size());
    }
}
