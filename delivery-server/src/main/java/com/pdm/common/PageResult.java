package com.pdm.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Data
public class PageResult<T> {

    private long total;
    private long page;
    private long size;
    private List<T> records;

    public static <T> PageResult<T> of(IPage<T> p) {
        PageResult<T> r = new PageResult<>();
        r.total = p.getTotal();
        r.page = p.getCurrent();
        r.size = p.getSize();
        r.records = p.getRecords() == null ? Collections.emptyList() : p.getRecords();
        return r;
    }

    public static <S, T> PageResult<T> of(IPage<S> p, Function<S, T> mapper) {
        PageResult<T> r = new PageResult<>();
        r.total = p.getTotal();
        r.page = p.getCurrent();
        r.size = p.getSize();
        r.records = (p.getRecords() == null ? Collections.<S>emptyList() : p.getRecords())
                .stream().map(mapper).toList();
        return r;
    }

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.page = page;
        r.size = size;
        r.records = records == null ? Collections.emptyList() : records;
        return r;
    }
}