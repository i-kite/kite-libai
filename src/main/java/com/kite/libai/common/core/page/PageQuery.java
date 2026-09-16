package com.kite.libai.common.core.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.Serializable;

/**
 * 分页查询入参基类,业务查询条件继承本类即可获得分页能力。
 *
 * @author kite
 */
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单页最大条数,防止前端传入过大值拖垮数据库 */
    private static final long MAX_PAGE_SIZE = 200L;

    private static final long DEFAULT_PAGE_SIZE = 10L;

    /** 页码,从 1 开始 */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    /**
     * 构造 MyBatis-Plus 分页对象,并对非法入参做兜底修正。
     *
     * @param <T> 分页记录类型
     */
    public <T> Page<T> toPage() {
        long current = (pageNum == null || pageNum < 1) ? 1L : pageNum;
        long size = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : pageSize;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        return new Page<>(current, size);
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
