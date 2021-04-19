package com.javatest.flowable.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 条件查询父类
 */
@Data
public class PageParams {

    private Long curPage;

    private Long limit;

    private String sidx;

    private String order;
}
