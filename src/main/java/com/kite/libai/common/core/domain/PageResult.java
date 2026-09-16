package com.kite.libai.common.core.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页结果封装。
 *
 * @param <T> 列表元素类型
 * @author kite
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> rows;

    public PageResult() {
        this.rows = new ArrayList<>();
    }

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 分页结果转换,用于把实体分页映射成 VO 分页。
     *
     * @param page      MyBatis-Plus 分页对象
     * @param converter 单条记录转换函数
     */
    public static <E, T> PageResult<T> of(IPage<E> page, Function<E, T> converter) {
        List<T> rows = page.getRecords() == null
                ? Collections.emptyList()
                : page.getRecords().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), rows);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, Collections.emptyList());
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
