package com.smartordering.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paged result holder.
 *
 * <p>MyBatis-Plus {@link IPage} serializes records as {@code records}/{@code current}/{@code size},
 * but the admin frontend expects {@code { list, pageNum, pageSize, total }}.
 * This wrapper bridges the two so controller output matches the frontend contract.</p>
 *
 * @author smartordering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    /** Current page records */
    private List<T> list;

    /** Page number (1-based) */
    private long pageNum;

    /** Page size */
    private long pageSize;

    /** Total number of records */
    private long total;

    /** Build from a MyBatis-Plus page whose records are already the desired VO type */
    public static <T> PageVO<T> of(IPage<T> page) {
        return new PageVO<>(page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal());
    }
}