package com.smartordering.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Paged result wrapper (ported from the reference project).
 *
 * <p>Serialization shape is {@code {pageNum, pageSize, total, pages, list}}, compatible with
 * the frontend {@code Api.System.PageResult}.</p>
 *
 * @author smartordering
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long pageNum;

    private Long pageSize;

    private Long total;

    private Long pages;

    private List<T> list;

    private PageResult() {
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setList(page.getRecords());
        return result;
    }

    public static <T> PageResult<T> of(List<T> list, Long pageNum, Long pageSize, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages(pageSize == null || pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize);
        result.setList(list);
        return result;
    }
}